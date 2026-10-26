package com.bornfire.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.bornfire.entities.BLRS_Guarantor_Entity;
import com.bornfire.entities.BLRS_Guarantor_Repo;
import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Repo;
import com.bornfire.entities.BLRS_ReminderGenerator_Entity;
import com.bornfire.entities.BLRS_ReminderGenerator_Repo;
import com.bornfire.entities.BLRS_ReminderParam_Entity;
import com.bornfire.entities.BLRS_Reminder_Entity;
import com.bornfire.entities.BLRS_Reminder_Repo;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Service for Reminder Generation (REM1, REM2, REM3).
 *
 * Ground Rules & Specifications:
 * - Rule 4: Duplicate protection: unique constraint on (accountNumber, reminderNo) in Reminder.
 * - Rule 8: Respects Email/SMS parameter flags and job/user requests.
 * - Rule 9: Configurable reminder age thresholds (reminder.threshold.rem*.min-age-days).
 * - Generates PDF reminder letters for loanee and guarantors packed into ZIP.
 * - Updates BLRS_Reminder_Entity and reflects back to BLRS_LoanAccountProfile_Entity.
 */
@Service
public class BLRS_ReminderGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(BLRS_ReminderGeneratorService.class);
    private static final int CHUNK_SIZE = 900;

    @Autowired
    private BLRS_ReminderGenerator_Repo reminderGenRepo;

    @Autowired
    private BLRS_Reminder_Repo reminderRepo;

    @Autowired
    private BLRS_LoanAccountProfile_Repo loanRepo;

    @Autowired
    private BLRS_Guarantor_Repo guarantorRepo;

    @Autowired
    private BLRS_DispatchDetailsService dispatchService;

    @Autowired
    private BLRS_ParameterService parameterService;

    @Value("${reminder.threshold.rem1.min-age-days:30}")
    private int rem1MinAge;

    @Value("${reminder.threshold.rem2.min-age-days:60}")
    private int rem2MinAge;

    @Value("${reminder.threshold.rem3.min-age-days:90}")
    private int rem3MinAge;

    @Value("${report.company.name:The Mauritius Civil Service Mutual Aid Association Ltd}")
    private String companyName;

    @Value("${report.company.brn:C10000071}")
    private String companyBrn;

    public List<BLRS_ReminderGenerator_Entity> getReminderList() {
        return reminderGenRepo.getReminderList();
    }

    public List<BLRS_ReminderGenerator_Entity> getSelectedReminders(List<String> accts) {
        if (accts == null || accts.isEmpty()) {
            return Collections.emptyList();
        }
        List<BLRS_ReminderGenerator_Entity> result = new ArrayList<>();
        for (int i = 0; i < accts.size(); i += CHUNK_SIZE) {
            List<String> chunk = accts.subList(i, Math.min(i + CHUNK_SIZE, accts.size()));
            result.addAll(reminderGenRepo.getSelectedReminders(chunk));
        }
        return result;
    }

    /**
     * Normalizes input strings (e.g. "Reminder-1", "Reminder 1", "REM1") to standard REM1/REM2/REM3.
     */
    public String normalizeReminderType(String reminderType) {
        if (reminderType == null) return "REM1";
        String s = reminderType.trim().toUpperCase();
        if (s.contains("3")) return "REM3";
        if (s.contains("2")) return "REM2";
        return "REM1";
    }

    /**
     * Evaluates reminder eligibility and checks for duplicate prevention (Rule 4).
     * Sequential reminder rules:
     * - Cannot generate duplicate of same reminder number.
     * - REM2 requires REM1 already generated.
     * - REM3 requires REM2 already generated.
     */
    public boolean canGenerateReminder(String accountNumber, String targetReminderNo) {
        if (!StringUtils.hasText(accountNumber) || !StringUtils.hasText(targetReminderNo)) {
            return false;
        }
        String acc = accountNumber.trim();
        String rem = normalizeReminderType(targetReminderNo);

        // Rule 4: Duplicate protection check
        if (reminderRepo.existsByAccountNumberAndReminderNo(acc, rem)) {
            log.info("Reminder skip: Account {} has already received {}.", acc, rem);
            return false;
        }

        List<String> issued = reminderRepo.findIssuedReminderTypesForAccount(acc);
        if ("REM2".equals(rem) && !issued.contains("REM1")) {
            log.info("Reminder skip: Account {} cannot receive REM2 before REM1.", acc);
            return false;
        }
        if ("REM3".equals(rem) && !issued.contains("REM2")) {
            log.info("Reminder skip: Account {} cannot receive REM3 before REM2.", acc);
            return false;
        }

        return true;
    }

    /**
     * Builds the ZIP package containing PDF reminder letters (Loanee + Guarantors)
     * and a summary CSV, and records reminder history into the database.
     */
    @Transactional
    public byte[] generateZip(List<String> accts, String reminderType, String email, String sms) throws IOException {
        String remType = normalizeReminderType(reminderType);
        String stamp = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());
        Date today = new Date();

        List<BLRS_ReminderGenerator_Entity> rawList = getSelectedReminders(accts);
        List<BLRS_ReminderGenerator_Entity> eligibleList = new ArrayList<>();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {

            for (BLRS_ReminderGenerator_Entity r : rawList) {
                String acc = r.getAcct_num();
                if (!StringUtils.hasText(acc)) continue;
                acc = acc.trim();

                // Check duplicate protection and eligibility
                if (!canGenerateReminder(acc, remType)) {
                    continue;
                }
                eligibleList.add(r);

                BLRS_LoanAccountProfile_Entity loan = loanRepo.findByAccountNumber(acc);
                if (loan == null) {
                    loan = loanRepo.findFirstByAccountNumber(acc);
                }

                BigDecimal overdue = r.getOverdue() != null ? r.getOverdue() : BigDecimal.ZERO;
                int ageDays = (loan != null && loan.getAge() != null) ? loan.getAge() : 0;

                // 1. Generate Loanee PDF letter
                byte[] loaneePdf = createLoaneeReminderPdf(remType, r, loan, overdue, today);
                String loaneeEntryName = remType + "_" + acc + "_Loanee.pdf";
                zos.putNextEntry(new ZipEntry(loaneeEntryName));
                zos.write(loaneePdf);
                zos.closeEntry();

                // 2. Generate Guarantor PDF letters
                List<BLRS_Guarantor_Entity> guarantors = guarantorRepo.findByAccountNumberOrderByGuarantorIdAsc(acc);
                StringBuilder guarantorNames = new StringBuilder();

                if (guarantors != null && !guarantors.isEmpty()) {
                    for (BLRS_Guarantor_Entity g : guarantors) {
                        if ("Y".equalsIgnoreCase(g.getGuarantorReminder())) {
                            byte[] gPdf = createGuarantorReminderPdf(remType, r, loan, g, overdue, today);
                            String gEntryName = remType + "_" + acc + "_Guarantor_" + g.getGuarantorId() + ".pdf";
                            zos.putNextEntry(new ZipEntry(gEntryName));
                            zos.write(gPdf);
                            zos.closeEntry();

                            if (guarantorNames.length() > 0) guarantorNames.append(", ");
                            guarantorNames.append(g.getGuarantorName() != null ? g.getGuarantorName() : g.getGuarantorId());
                        }
                    }
                }

                // 3. Persist BLRS_Reminder_Entity record (Rule 4 enforced by repo & unique constraint)
                String remId = "REM-" + remType + "-" + acc + "-" + new SimpleDateFormat("yyyyMMdd").format(today);
                BLRS_Reminder_Entity remEntity = new BLRS_Reminder_Entity(
                        remId, acc, remType, today, overdue, ageDays,
                        r.getAcct_name() != null ? r.getAcct_name() : (loan != null ? loan.getBorrowerName() : ""),
                        guarantorNames.toString(), loaneeEntryName,
                        "Y".equalsIgnoreCase(email) ? "Y" : "N",
                        "Y".equalsIgnoreCase(sms) ? "Y" : "N"
                );
                remEntity.setCustomerId(r.getCust_id());
                remEntity.setSchemeCode(r.getSchem_code());
                remEntity.setDepartment(r.getDept());
                reminderRepo.save(remEntity);

                // 4. Update BLRS_LoanAccountProfile_Entity
                if (loan != null) {
                    loan.setReminderNumber(remType);
                    loan.setReminderDate(LocalDate.now());
                    loan.setReminderOverdues(overdue);
                    loan.setReminderLoanee(r.getAcct_name() != null ? r.getAcct_name() : loan.getBorrowerName());
                    if (guarantorNames.length() > 0) {
                        loan.setReminderGuarantor(guarantorNames.toString());
                    }
                    loanRepo.save(loan);
                }
            }

            // 5. Add Summary CSV into the ZIP
            String csv = generateCsvFromList(eligibleList, remType, email, sms);
            zos.putNextEntry(new ZipEntry("reminders_" + stamp + ".csv"));
            zos.write(csv.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        // 6. Record into dispatch details table
        if (!eligibleList.isEmpty()) {
            dispatchService.recordReminderGeneration(eligibleList, remType, today);
        }

        return bos.toByteArray();
    }

    public String generateCsv(List<String> accts, String reminderType, String email, String sms) {
        return generateCsvFromList(getSelectedReminders(accts), reminderType, email, sms);
    }

    private String generateCsvFromList(List<BLRS_ReminderGenerator_Entity> list, String reminderType, String email, String sms) {
        StringBuilder sb = new StringBuilder();
        sb.append("SCHEME CODE,CUSTOMER ID,ACCOUNT NUMBER,NAME OF BORROWER,OVERDUE,DEPARTMENT,")
          .append("REMINDER SENT,REMINDER TYPE,EMAIL,SMS\n");

        for (BLRS_ReminderGenerator_Entity r : list) {
            sb.append(nz(r.getSchem_code())).append(',')
              .append(nz(r.getCust_id())).append(',')
              .append(nz(r.getAcct_num())).append(',')
              .append('"').append(safe(r.getAcct_name())).append('"').append(',')
              .append(r.getOverdue() == null ? "0.00" : r.getOverdue().toPlainString()).append(',')
              .append(nz(r.getDept())).append(',')
              .append(nz(r.getRem_send())).append(',')
              .append(nz(reminderType)).append(',')
              .append(nz(email)).append(',')
              .append(nz(sms)).append('\n');
        }
        return sb.toString();
    }

    /**
     * Creates a formal PDF reminder notice for the borrower using iText.
     */
    private byte[] createLoaneeReminderPdf(String remType, BLRS_ReminderGenerator_Entity r,
                                           BLRS_LoanAccountProfile_Entity loan, BigDecimal overdue, Date date) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED);
            Font fBody = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            Paragraph p = new Paragraph(companyName, fTitle);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            p = new Paragraph("Business Registration No (BRN): " + companyBrn, fSub);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(15);
            doc.add(p);

            String remTitle = "REMINDER NOTICE " + remType.replace("REM", "");
            p = new Paragraph(remTitle + " - NOTICE OF OVERDUE LOAN ARREARS", fHead);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(20);
            doc.add(p);

            SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
            doc.add(new Paragraph("Date: " + df.format(date), fBody));
            doc.add(new Paragraph("Account Number: " + nz(r.getAcct_num()), fBold));
            doc.add(new Paragraph("Customer ID: " + nz(r.getCust_id()), fBody));
            doc.add(new Paragraph("Borrower Name: " + nz(r.getAcct_name()), fBold));
            if (loan != null && StringUtils.hasText(loan.getMailingAddress())) {
                doc.add(new Paragraph("Mailing Address: " + loan.getMailingAddress(), fBody));
            }
            doc.add(new Paragraph("Scheme: " + nz(r.getSchem_code()) + "   Department: " + nz(r.getDept()), fBody));
            doc.add(new Paragraph(" ", fBody));

            DecimalFormat mf = new DecimalFormat("#,##0.00");
            p = new Paragraph("Dear Sir/Madam,\n\n"
                    + "According to our records, your loan account referenced above is currently in arrears.\n"
                    + "Total Overdue Arrears: Rs. " + mf.format(overdue) + "\n\n"
                    + "You are hereby requested to arrange immediate settlement of the overdue amount to avoid "
                    + "further administrative escalation, reporting, and legal recovery proceedings.\n\n"
                    + "Payment may be made through direct debit, online banking, or at any of our branches.\n\n"
                    + "Yours faithfully,\n\n"
                    + "Loan Recovery Department\n"
                    + companyName, fBody);
            p.setSpacingBefore(10);
            doc.add(p);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate loanee PDF letter for {}", r.getAcct_num(), e);
            return new byte[0];
        }
    }

    /**
     * Creates a formal PDF reminder notice for a loan guarantor using iText.
     */
    private byte[] createGuarantorReminderPdf(String remType, BLRS_ReminderGenerator_Entity r,
                                              BLRS_LoanAccountProfile_Entity loan, BLRS_Guarantor_Entity g,
                                              BigDecimal overdue, Date date) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(180, 0, 0));
            Font fBody = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            Paragraph p = new Paragraph(companyName, fTitle);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);

            p = new Paragraph("Business Registration No (BRN): " + companyBrn, fSub);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(15);
            doc.add(p);

            p = new Paragraph("NOTICE TO GUARANTOR - LOAN IN ARREARS (" + remType + ")", fHead);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(20);
            doc.add(p);

            SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
            doc.add(new Paragraph("Date: " + df.format(date), fBody));
            doc.add(new Paragraph("To Guarantor: " + nz(g.getGuarantorName()) + " (ID: " + nz(g.getGuarantorId()) + ")", fBold));
            if (StringUtils.hasText(g.getMailingAddress())) {
                doc.add(new Paragraph("Address: " + g.getMailingAddress(), fBody));
            }
            doc.add(new Paragraph("Loan Account: " + nz(r.getAcct_num()), fBold));
            doc.add(new Paragraph("Principal Borrower: " + nz(r.getAcct_name()), fBold));
            doc.add(new Paragraph(" ", fBody));

            DecimalFormat mf = new DecimalFormat("#,##0.00");
            p = new Paragraph("Dear Sir/Madam,\n\n"
                    + "As guarantor for the above referenced loan facility granted to " + nz(r.getAcct_name()) + ",\n"
                    + "please be advised that the borrower has failed to pay the required instalments.\n"
                    + "Current Overdue Arrears: Rs. " + mf.format(overdue) + "\n\n"
                    + "Under the terms of the guarantee agreement, you are jointly and severally liable for this facility.\n"
                    + "Please contact the borrower to ensure prompt settlement, failing which recovery action may be taken.\n\n"
                    + "Yours faithfully,\n\n"
                    + "Loan Recovery Department\n"
                    + companyName, fBody);
            p.setSpacingBefore(10);
            doc.add(p);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate guarantor PDF letter for {}", r.getAcct_num(), e);
            return new byte[0];
        }
    }

    private String safe(String s) {
        String v = nz(s);
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return v.replace("\"", "\"\"");
    }

    private String nz(String s) {
        return s == null ? "" : s.trim();
    }
}