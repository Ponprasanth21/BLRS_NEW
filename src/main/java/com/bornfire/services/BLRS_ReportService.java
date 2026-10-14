package com.bornfire.services;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bornfire.entities.BLRS_LoanAccountProfile_Entity;
import com.bornfire.entities.BLRS_LoanAccountProfile_Repo;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Reports menu (manual page 70-78) : 3 PDF reports.
 *   6.1 List of Overdue Reports    -> ListOfOverdue.pdf
 *   6.2 List of Reminder Reports   -> ListOfReminders.pdf
 *   6.3 List of Guarantor Reports  -> ListOfGuarantors.pdf
 * PDF is drawn with iText 2.1.7 (com.lowagie) - it already comes with jasperreports 6.21.3 in your pom,
 * so NO new dependency is needed.
 */
@Service
@Transactional(readOnly = true)
public class BLRS_ReportService {

	@Autowired
	private BLRS_LoanAccountProfile_Repo loanRepo;

	@Value("${report.company.name:The Mauritius Civil Service Mutual Aid Association Ltd}")
	private String companyName;

	@Value("${report.company.brn:C10000071}")
	private String companyBrn;

	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
	private static final Color HEAD_BG = new Color(189, 215, 238);
	private static final int L = Element.ALIGN_LEFT;
	private static final int R = Element.ALIGN_RIGHT;

	// ------------------------------------------------------------------
	// Drop-downs
	// ------------------------------------------------------------------
	public List<String> getSchemeCodes() {
		return loanRepo.getSchemeCodes();
	}

	public List<String> getDepartments() {
		return loanRepo.getDepartments();
	}

	// ------------------------------------------------------------------
	// 6.1 Overdue report
	// ------------------------------------------------------------------
	public byte[] overduePdf(int month, int year, String scheme, String dept) {
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1);

		List<String[]> rows = new ArrayList<String[]>();
		for (BLRS_LoanAccountProfile_Entity r : loanRepo
				.findByOverdueRecordDateGreaterThanEqualAndOverdueRecordDateLessThanOrderBySchemeAscCustomerIdAsc(from,
						to)) {
			if (!match(r.getScheme(), scheme) || !match(r.getOverdueDepartment(), dept)) {
				continue;
			}
			rows.add(new String[] { nz(r.getCustomerId()), nz(r.getBorrowerName()), nz(r.getScheme()),
					nz(r.getAccountNumber()), money(r.getTotalArrears()), dueDate(r) });
		}
		return build("List Of Overdue Accounts", month, year,
				new String[] { "CUSTOMER ID", "CUSTOMER NAME", "SCHEME CODE", "ACCOUNT NUMBER", "OVERDUES",
						"DUE DATE" },
				new float[] { 14, 30, 12, 22, 12, 12 }, new int[] { L, L, L, L, R, L }, rows, false);
	}

	// ------------------------------------------------------------------
	// 6.2 Reminder report
	// ------------------------------------------------------------------
	public byte[] reminderPdf(int month, int year, String reminderType, String scheme, String dept) {
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1);

		List<String[]> rows = new ArrayList<String[]>();
		for (BLRS_LoanAccountProfile_Entity r : loanRepo
				.findByReminderDateGreaterThanEqualAndReminderDateLessThanOrderByReminderDateAscCustomerIdAsc(from,
						to)) {
			if (!sameReminder(r.getReminderNumber(), reminderType) || !match(r.getScheme(), scheme)
					|| !match(r.getOverdueDepartment(), dept)) {
				continue;
			}
			BigDecimal overdue = r.getReminderOverdues() != null ? r.getReminderOverdues() : r.getTotalArrears();
			rows.add(new String[] { nz(r.getReminderNumber()), date(r.getReminderDate()), nz(r.getCustomerId()),
					nz(r.getBorrowerName()), nz(r.getScheme()), nz(r.getAccountNumber()), money(overdue),
					nz(r.getCourierPostalReference()), nz(r.getCourierPostalAcknowledgement()) });
		}
		return build("List Of Reminders", month, year,
				new String[] { "REMINDER TYPE", "REMINDER DATE", "CUST ID", "NAME OF BORROWER", "SCHEME CODE",
						"ACCOUNT NO.", "OVERDUES", "COURIER REF", "COURIER ACK" },
				new float[] { 11, 11, 10, 22, 9, 15, 10, 11, 11 }, new int[] { L, L, L, L, L, L, R, L, L }, rows,
				true);
	}

	// ------------------------------------------------------------------
	// 6.3 Guarantor report
	// ------------------------------------------------------------------
	public byte[] guarantorPdf(int month, int year, String reminderType, String scheme, String dept) {
		LocalDate from = LocalDate.of(year, month, 1);
		LocalDate to = from.plusMonths(1);

		List<String[]> rows = new ArrayList<String[]>();
		for (BLRS_LoanAccountProfile_Entity r : loanRepo
				.findByReminderDateGreaterThanEqualAndReminderDateLessThanOrderByReminderDateAscCustomerIdAsc(from,
						to)) {
			if (isBlank(r.getReminderGuarantor()) || !sameReminder(r.getReminderNumber(), reminderType)
					|| !match(r.getScheme(), scheme) || !match(r.getOverdueDepartment(), dept)) {
				continue;
			}
			String loanee = !isBlank(r.getReminderLoanee()) ? r.getReminderLoanee() : r.getBorrowerName();
			rows.add(new String[] { nz(r.getAccountNumber()), nz(loanee), nz(r.getReminderGuarantor()),
					money(r.getLoanOutstandingBalance()), nz(r.getReminderNumber()), date(r.getReminderDate()) });
		}
		return build("List Of Guarantors", month, year,
				new String[] { "LOAN NO", "NAME OF LOANEE", "NAME OF GUARANTOR", "OUTSTANDING BALANCE", "REMINDER",
						"REMINDER DATE" },
				new float[] { 18, 24, 24, 14, 10, 10 }, new int[] { L, L, L, R, L, L }, rows, false);
	}

	// ------------------------------------------------------------------
	// PDF layout (same look as the manual : company, BRN, title + report date, blue header row)
	// ------------------------------------------------------------------
	private byte[] build(String title, int month, int year, String[] heads, float[] widths, int[] align,
			List<String[]> rows, boolean landscape) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Document doc = new Document(landscape ? PageSize.A4.rotate() : PageSize.A4, 30, 30, 30, 40);
			PdfWriter writer = PdfWriter.getInstance(doc, out);
			writer.setPageEvent(new PageNumbers());
			doc.open();

			Font fCompany = FontFactory.getFont(FontFactory.HELVETICA, 14);
			Font fBrn = FontFactory.getFont(FontFactory.HELVETICA, 10);
			Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 9);
			Font fHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
			Font fCell = FontFactory.getFont(FontFactory.HELVETICA, 8);

			Paragraph p = new Paragraph(companyName, fCompany);
			p.setAlignment(Element.ALIGN_CENTER);
			doc.add(p);

			p = new Paragraph("BRN :  " + companyBrn, fBrn);
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(14);
			doc.add(p);

			// "List Of ... for the Month of January, 2019"          "Report Date : January 10, 2019"
			PdfPTable sub = new PdfPTable(new float[] { 70, 30 });
			sub.setWidthPercentage(100);
			sub.setSpacingAfter(8);
			PdfPCell left = new PdfPCell(new Phrase(title + " for the Month of  " + monthName(month) + ", " + year, fSub));
			left.setBorder(Rectangle.NO_BORDER);
			PdfPCell right = new PdfPCell(new Phrase(
					"Report Date :  " + new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(new Date()), fSub));
			right.setBorder(Rectangle.NO_BORDER);
			right.setHorizontalAlignment(Element.ALIGN_RIGHT);
			sub.addCell(left);
			sub.addCell(right);
			doc.add(sub);

			PdfPTable t = new PdfPTable(widths);
			t.setWidthPercentage(100);
			if (!rows.isEmpty()) {
				t.setHeaderRows(1); // repeat the blue header on every page
			}
			for (String h : heads) {
				PdfPCell c = new PdfPCell(new Phrase(h, fHead));
				c.setBackgroundColor(HEAD_BG);
				c.setPadding(5);
				t.addCell(c);
			}
			for (String[] r : rows) {
				for (int i = 0; i < r.length; i++) {
					PdfPCell c = new PdfPCell(new Phrase(r[i], fCell));
					c.setPadding(4);
					c.setHorizontalAlignment(align[i]);
					t.addCell(c);
				}
			}
			doc.add(t);

			if (rows.isEmpty()) {
				Paragraph none = new Paragraph("No records found for the selected criteria.", fSub);
				none.setSpacingBefore(10);
				doc.add(none);
			}

			doc.close();
			return out.toByteArray();
		} catch (DocumentException e) {
			throw new IllegalStateException("Report could not be generated", e);
		}
	}

	/** "Page n" at the bottom right of every page */
	private static class PageNumbers extends PdfPageEventHelper {
		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			try {
				PdfContentByte cb = writer.getDirectContent();
				BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
				cb.beginText();
				cb.setFontAndSize(bf, 8);
				cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Page " + writer.getPageNumber(), document.right(),
						document.bottom() - 16, 0);
				cb.endText();
			} catch (Exception e) {
				// page number is only decoration
			}
		}
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	/**
	 * DUE DATE column. The loan table has no "first due date" column, so it is worked out as
	 * OVERDUE_RECORD_DATE minus OVERDUE_MONTH_ARREARS months (ASSUMPTION - change here if you have a real column).
	 */
	private String dueDate(BLRS_LoanAccountProfile_Entity r) {
		LocalDate d = r.getOverdueRecordDate();
		if (d == null) {
			return "";
		}
		Integer m = r.getOverdueMonthArrears();
		if (m != null && m > 0) {
			d = d.minusMonths(m);
		}
		return d.format(DATE_FMT);
	}

	/** empty filter / "ALL" = no filter */
	private boolean match(String value, String filter) {
		if (isBlank(filter) || "ALL".equalsIgnoreCase(filter.trim())) {
			return true;
		}
		return value != null && value.trim().equalsIgnoreCase(filter.trim());
	}

	/** Reminder-1 = REM1 = reminder 1 */
	private boolean sameReminder(String actual, String wanted) {
		if (isBlank(wanted)) {
			return true;
		}
		return reminderNo(actual).equals(reminderNo(wanted));
	}

	private String reminderNo(String s) {
		if (s == null) {
			return "";
		}
		String digits = s.replaceAll("[^0-9]", "");
		return digits.isEmpty() ? s.trim().toUpperCase() : digits;
	}

	private String monthName(int month) {
		return new DateFormatSymbols(Locale.ENGLISH).getMonths()[month - 1];
	}

	private String money(BigDecimal v) {
		if (v == null) {
			return "0.00";
		}
		return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(v);
	}

	private String date(LocalDate d) {
		return d == null ? "" : d.format(DATE_FMT);
	}

	private String nz(String s) {
		return s == null ? "" : s;
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}
}