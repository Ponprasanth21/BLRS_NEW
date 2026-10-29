package com.bornfire.entities;


import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "blrs_loan_account_profile_tab", schema = "blrs")
public class BLRS_LoanAccountProfile_Entity {

    // Loanee Details
    @Id
    @Column(name = "CUSTOMER_ID", length = 50)
    private String customerId;

    @Column(name = "ACCOUNT_NUMBER", length = 50)
    private String accountNumber;

    @Column(name = "BORROWER_NAME", length = 200)
    private String borrowerName;

    @Column(name = "MAILING_ADDRESS", length = 500)
    private String mailingAddress;

    @Column(name = "EMAIL_ID", length = 200)
    private String emailId;

    @Column(name = "MOBILE_NO", length = 20)
    private String mobileNo;

    @Column(name = "LOANEE_REMINDER")
    private String loaneeReminder;

    // Loan Master Details
    @Column(name = "SCHEME", length = 50)
    private String scheme;

    @Column(name = "LOAN_DISBURSEMENT_DATE")
    private LocalDate loanDisbursementDate;

    @Column(name = "LOAN_AMOUNT_APPLIED", precision = 20, scale = 2)
    private BigDecimal loanAmountApplied;

    @Column(name = "LOAN_OUTSTANDING_BALANCE", precision = 20, scale = 2)
    private BigDecimal loanOutstandingBalance;

    @Column(name = "LOAN_CAPITAL_ARREARS", precision = 20, scale = 2)
    private BigDecimal loanCapitalArrears;

    @Column(name = "LOAN_INTEREST_ARREARS", precision = 20, scale = 2)
    private BigDecimal loanInterestArrears;

    @Column(name = "NUMBER_OF_GUARANTORS")
    private Integer numberOfGuarantors;

    @Column(name = "ACTUAL_INTEREST_RATE", precision = 10, scale = 4)
    private BigDecimal actualInterestRate;

    @Column(name = "NUMBER_OF_MONTHS_IN_ARREARS")
    private Integer numberOfMonthsInArrears;

    @Column(name = "PAYMENTS_RECEIVED", precision = 20, scale = 2)
    private BigDecimal paymentsReceived;

    // Overdues Details
    @Column(name = "OVERDUE_DEPARTMENT", length = 100)
    private String overdueDepartment;

    @Column(name = "OVERDUE_RECORD_DATE")
    private LocalDate overdueRecordDate;

    @Column(name = "OVERDUE_DISBURSEMENT_DATE")
    private LocalDate overdueDisbursementDate;

    @Column(name = "OVERDUE_LOAN_AMOUNT", precision = 20, scale = 2)
    private BigDecimal overdueLoanAmount;

    @Column(name = "OVERDUE_OUTSTANDING_BALANCE", precision = 20, scale = 2)
    private BigDecimal overdueOutstandingBalance;

    @Column(name = "OVERDUE_INTEREST_RATE", precision = 10, scale = 4)
    private BigDecimal overdueInterestRate;

    @Column(name = "TOTAL_ARREARS", precision = 20, scale = 2)
    private BigDecimal totalArrears;

    @Column(name = "OVERDUE_CAPITAL_ARREARS", precision = 20, scale = 2)
    private BigDecimal overdueCapitalArrears;

    @Column(name = "OVERDUE_INTEREST_ARREARS", precision = 20, scale = 2)
    private BigDecimal overdueInterestArrears;

    @Column(name = "OVERDUE_MONTH_ARREARS")
    private Integer overdueMonthArrears;

    // Reminder Details
    @Column(name = "REMINDER_SL_NO")
    private Integer reminderSlNo;

    @Column(name = "REMINDER_OVERDUES", precision = 20, scale = 2)
    private BigDecimal reminderOverdues;

    @Column(name = "REMINDER_AGE")
    private Integer reminderAge;

    @Column(name = "REMINDER_NUMBER", length = 50)
    private String reminderNumber;

    @Column(name = "REMINDER_DATE")
    private LocalDate reminderDate;

    @Column(name = "COURIER_POSTAL_REFERENCE", length = 200)
    private String courierPostalReference;

    @Column(name = "COURIER_POSTAL_ACKNOWLEDGEMENT", length = 200)
    private String courierPostalAcknowledgement;

    @Column(name = "REMINDER_LOANEE", length = 200)
    private String reminderLoanee;

    @Column(name = "REMINDER_GUARANTOR", length = 200)
    private String reminderGuarantor;

    // Recovery Details
    @Column(name = "RECOVERY_RECORD_DATE")
    private LocalDate recoveryRecordDate;

    @Column(name = "RECOVERY_LOAN_OUTSTANDING", precision = 20, scale = 2)
    private BigDecimal recoveryLoanOutstanding;

    @Column(name = "RECOVERY_PRINCIPAL", precision = 20, scale = 2)
    private BigDecimal recoveryPrincipal;

    @Column(name = "RECOVERY_INTEREST", precision = 20, scale = 2)
    private BigDecimal recoveryInterest;

    @Column(name = "RECOVERY_TOTAL", precision = 20, scale = 2)
    private BigDecimal recoveryTotal;

    @Column(name = "RECOVERY_REMINDER_DATE")
    private LocalDate recoveryReminderDate;

    @Column(name = "RECOVERY_DATE")
    private LocalDate recoveryDate;

    @Column(name = "RECOVERY_AMOUNT", precision = 20, scale = 2)
    private BigDecimal recoveryAmount;

    @Column(name = "RECOVERY_OVERDUES", precision = 20, scale = 2)
    private BigDecimal recoveryOverdues;

    @Column(name = "RECOVERY_AGE")
    private Integer recoveryAge;

    @Column(name = "RECOVERY_STATUS", length = 50)
    private String recoveryStatus;

    

    // Guarantor Details (Guarantor 1 and 2)
    @Column(name = "GUARANTOR1_CUSTOMER_ID", length = 50)
    private String guarantor1CustomerId;
 
    @Column(name = "GUARANTOR1_NAME", length = 200)
    private String guarantor1Name;
 
    @Column(name = "GUARANTOR1_NIC", length = 50)
    private String guarantor1Nic;
 
    @Column(name = "GUARANTOR1_MAILING_ADDRESS", length = 500)
    private String guarantor1MailingAddress;
 
    @Column(name = "GUARANTOR1_EMAIL_ID", length = 200)
    private String guarantor1EmailId;
 
    @Column(name = "GUARANTOR1_MOBILE_NO", length = 20)
    private String guarantor1MobileNo;
 
    @Column(name = "GUARANTOR1_REMINDER", length = 10)
    private String guarantor1Reminder;
 
    @Column(name = "GUARANTOR2_CUSTOMER_ID", length = 50)
    private String guarantor2CustomerId;
 
    @Column(name = "GUARANTOR2_NAME", length = 200)
    private String guarantor2Name;
 
    @Column(name = "GUARANTOR2_NIC", length = 50)
    private String guarantor2Nic;
 
    @Column(name = "GUARANTOR2_MAILING_ADDRESS", length = 500)
    private String guarantor2MailingAddress;
 
    @Column(name = "GUARANTOR2_EMAIL_ID", length = 200)
    private String guarantor2EmailId;
 
    @Column(name = "GUARANTOR2_MOBILE_NO", length = 20)
    private String guarantor2MobileNo;
 
    @Column(name = "GUARANTOR2_REMINDER", length = 10)
    private String guarantor2Reminder;
    // Constructors
    public BLRS_LoanAccountProfile_Entity() {
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public String getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(String mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getLoaneeReminder() {
        return loaneeReminder;
    }

    public void setLoaneeReminder(String loaneeReminder) {
        this.loaneeReminder = loaneeReminder;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public LocalDate getLoanDisbursementDate() {
        return loanDisbursementDate;
    }

    public void setLoanDisbursementDate(LocalDate loanDisbursementDate) {
        this.loanDisbursementDate = loanDisbursementDate;
    }

    public BigDecimal getLoanAmountApplied() {
        return loanAmountApplied;
    }

    public void setLoanAmountApplied(BigDecimal loanAmountApplied) {
        this.loanAmountApplied = loanAmountApplied;
    }

    public BigDecimal getLoanOutstandingBalance() {
        return loanOutstandingBalance;
    }

    public void setLoanOutstandingBalance(BigDecimal loanOutstandingBalance) {
        this.loanOutstandingBalance = loanOutstandingBalance;
    }

    public BigDecimal getLoanCapitalArrears() {
        return loanCapitalArrears;
    }

    public void setLoanCapitalArrears(BigDecimal loanCapitalArrears) {
        this.loanCapitalArrears = loanCapitalArrears;
    }

    public BigDecimal getLoanInterestArrears() {
        return loanInterestArrears;
    }

    public void setLoanInterestArrears(BigDecimal loanInterestArrears) {
        this.loanInterestArrears = loanInterestArrears;
    }

    public Integer getNumberOfGuarantors() {
        return numberOfGuarantors;
    }

    public void setNumberOfGuarantors(Integer numberOfGuarantors) {
        this.numberOfGuarantors = numberOfGuarantors;
    }

    public BigDecimal getActualInterestRate() {
        return actualInterestRate;
    }

    public void setActualInterestRate(BigDecimal actualInterestRate) {
        this.actualInterestRate = actualInterestRate;
    }

    public Integer getNumberOfMonthsInArrears() {
        return numberOfMonthsInArrears;
    }

    public void setNumberOfMonthsInArrears(Integer numberOfMonthsInArrears) {
        this.numberOfMonthsInArrears = numberOfMonthsInArrears;
    }

    public BigDecimal getPaymentsReceived() {
        return paymentsReceived;
    }

    public void setPaymentsReceived(BigDecimal paymentsReceived) {
        this.paymentsReceived = paymentsReceived;
    }

    public String getOverdueDepartment() {
        return overdueDepartment;
    }

    public void setOverdueDepartment(String overdueDepartment) {
        this.overdueDepartment = overdueDepartment;
    }

    public LocalDate getOverdueRecordDate() {
        return overdueRecordDate;
    }

    public void setOverdueRecordDate(LocalDate overdueRecordDate) {
        this.overdueRecordDate = overdueRecordDate;
    }

    public LocalDate getOverdueDisbursementDate() {
        return overdueDisbursementDate;
    }

    public void setOverdueDisbursementDate(LocalDate overdueDisbursementDate) {
        this.overdueDisbursementDate = overdueDisbursementDate;
    }

    public BigDecimal getOverdueLoanAmount() {
        return overdueLoanAmount;
    }

    public void setOverdueLoanAmount(BigDecimal overdueLoanAmount) {
        this.overdueLoanAmount = overdueLoanAmount;
    }

    public BigDecimal getOverdueOutstandingBalance() {
        return overdueOutstandingBalance;
    }

    public void setOverdueOutstandingBalance(BigDecimal overdueOutstandingBalance) {
        this.overdueOutstandingBalance = overdueOutstandingBalance;
    }

    public BigDecimal getOverdueInterestRate() {
        return overdueInterestRate;
    }

    public void setOverdueInterestRate(BigDecimal overdueInterestRate) {
        this.overdueInterestRate = overdueInterestRate;
    }

    public BigDecimal getTotalArrears() {
        return totalArrears;
    }

    public void setTotalArrears(BigDecimal totalArrears) {
        this.totalArrears = totalArrears;
    }

    public BigDecimal getOverdueCapitalArrears() {
        return overdueCapitalArrears;
    }

    public void setOverdueCapitalArrears(BigDecimal overdueCapitalArrears) {
        this.overdueCapitalArrears = overdueCapitalArrears;
    }

    public BigDecimal getOverdueInterestArrears() {
        return overdueInterestArrears;
    }

    public void setOverdueInterestArrears(BigDecimal overdueInterestArrears) {
        this.overdueInterestArrears = overdueInterestArrears;
    }

    public Integer getOverdueMonthArrears() {
        return overdueMonthArrears;
    }

    public void setOverdueMonthArrears(Integer overdueMonthArrears) {
        this.overdueMonthArrears = overdueMonthArrears;
    }

    public Integer getReminderSlNo() {
        return reminderSlNo;
    }

    public void setReminderSlNo(Integer reminderSlNo) {
        this.reminderSlNo = reminderSlNo;
    }

    public BigDecimal getReminderOverdues() {
        return reminderOverdues;
    }

    public void setReminderOverdues(BigDecimal reminderOverdues) {
        this.reminderOverdues = reminderOverdues;
    }

    public Integer getReminderAge() {
        return reminderAge;
    }

    public void setReminderAge(Integer reminderAge) {
        this.reminderAge = reminderAge;
    }

    public String getReminderNumber() {
        return reminderNumber;
    }

    public void setReminderNumber(String reminderNumber) {
        this.reminderNumber = reminderNumber;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getCourierPostalReference() {
        return courierPostalReference;
    }

    public void setCourierPostalReference(String courierPostalReference) {
        this.courierPostalReference = courierPostalReference;
    }

    public String getCourierPostalAcknowledgement() {
        return courierPostalAcknowledgement;
    }

    public void setCourierPostalAcknowledgement(String courierPostalAcknowledgement) {
        this.courierPostalAcknowledgement = courierPostalAcknowledgement;
    }

    public String getReminderLoanee() {
        return reminderLoanee;
    }

    public void setReminderLoanee(String reminderLoanee) {
        this.reminderLoanee = reminderLoanee;
    }

    public String getReminderGuarantor() {
        return reminderGuarantor;
    }

    public void setReminderGuarantor(String reminderGuarantor) {
        this.reminderGuarantor = reminderGuarantor;
    }

    public LocalDate getRecoveryRecordDate() {
        return recoveryRecordDate;
    }

    public void setRecoveryRecordDate(LocalDate recoveryRecordDate) {
        this.recoveryRecordDate = recoveryRecordDate;
    }

    public BigDecimal getRecoveryLoanOutstanding() {
        return recoveryLoanOutstanding;
    }

    public void setRecoveryLoanOutstanding(BigDecimal recoveryLoanOutstanding) {
        this.recoveryLoanOutstanding = recoveryLoanOutstanding;
    }

    public BigDecimal getRecoveryPrincipal() {
        return recoveryPrincipal;
    }

    public void setRecoveryPrincipal(BigDecimal recoveryPrincipal) {
        this.recoveryPrincipal = recoveryPrincipal;
    }

    public BigDecimal getRecoveryInterest() {
        return recoveryInterest;
    }

    public void setRecoveryInterest(BigDecimal recoveryInterest) {
        this.recoveryInterest = recoveryInterest;
    }

    public BigDecimal getRecoveryTotal() {
        return recoveryTotal;
    }

    public void setRecoveryTotal(BigDecimal recoveryTotal) {
        this.recoveryTotal = recoveryTotal;
    }

    public LocalDate getRecoveryReminderDate() {
        return recoveryReminderDate;
    }

    public void setRecoveryReminderDate(LocalDate recoveryReminderDate) {
        this.recoveryReminderDate = recoveryReminderDate;
    }

    public LocalDate getRecoveryDate() {
        return recoveryDate;
    }

    public void setRecoveryDate(LocalDate recoveryDate) {
        this.recoveryDate = recoveryDate;
    }

    public BigDecimal getRecoveryAmount() {
        return recoveryAmount;
    }

    public void setRecoveryAmount(BigDecimal recoveryAmount) {
        this.recoveryAmount = recoveryAmount;
    }

    public BigDecimal getRecoveryOverdues() {
        return recoveryOverdues;
    }

    public void setRecoveryOverdues(BigDecimal recoveryOverdues) {
        this.recoveryOverdues = recoveryOverdues;
    }

    public Integer getRecoveryAge() {
        return recoveryAge;
    }

    public void setRecoveryAge(Integer recoveryAge) {
        this.recoveryAge = recoveryAge;
    }

    public String getRecoveryStatus() {
        return recoveryStatus;
    }

    public void setRecoveryStatus(String recoveryStatus) {
        this.recoveryStatus = recoveryStatus;
    }

	public String getGuarantor1CustomerId() {
		return guarantor1CustomerId;
	}

	public void setGuarantor1CustomerId(String guarantor1CustomerId) {
		this.guarantor1CustomerId = guarantor1CustomerId;
	}

	public String getGuarantor1Name() {
		return guarantor1Name;
	}

	public void setGuarantor1Name(String guarantor1Name) {
		this.guarantor1Name = guarantor1Name;
	}

	public String getGuarantor1Nic() {
		return guarantor1Nic;
	}

	public void setGuarantor1Nic(String guarantor1Nic) {
		this.guarantor1Nic = guarantor1Nic;
	}

	public String getGuarantor1MailingAddress() {
		return guarantor1MailingAddress;
	}

	public void setGuarantor1MailingAddress(String guarantor1MailingAddress) {
		this.guarantor1MailingAddress = guarantor1MailingAddress;
	}

	public String getGuarantor1EmailId() {
		return guarantor1EmailId;
	}

	public void setGuarantor1EmailId(String guarantor1EmailId) {
		this.guarantor1EmailId = guarantor1EmailId;
	}

	public String getGuarantor1MobileNo() {
		return guarantor1MobileNo;
	}

	public void setGuarantor1MobileNo(String guarantor1MobileNo) {
		this.guarantor1MobileNo = guarantor1MobileNo;
	}

	public String getGuarantor1Reminder() {
		return guarantor1Reminder;
	}

	public void setGuarantor1Reminder(String guarantor1Reminder) {
		this.guarantor1Reminder = guarantor1Reminder;
	}

	public String getGuarantor2CustomerId() {
		return guarantor2CustomerId;
	}

	public void setGuarantor2CustomerId(String guarantor2CustomerId) {
		this.guarantor2CustomerId = guarantor2CustomerId;
	}

	public String getGuarantor2Name() {
		return guarantor2Name;
	}

	public void setGuarantor2Name(String guarantor2Name) {
		this.guarantor2Name = guarantor2Name;
	}

	public String getGuarantor2Nic() {
		return guarantor2Nic;
	}

	public void setGuarantor2Nic(String guarantor2Nic) {
		this.guarantor2Nic = guarantor2Nic;
	}

	public String getGuarantor2MailingAddress() {
		return guarantor2MailingAddress;
	}

	public void setGuarantor2MailingAddress(String guarantor2MailingAddress) {
		this.guarantor2MailingAddress = guarantor2MailingAddress;
	}

	public String getGuarantor2EmailId() {
		return guarantor2EmailId;
	}

	public void setGuarantor2EmailId(String guarantor2EmailId) {
		this.guarantor2EmailId = guarantor2EmailId;
	}

	public String getGuarantor2MobileNo() {
		return guarantor2MobileNo;
	}

	public void setGuarantor2MobileNo(String guarantor2MobileNo) {
		this.guarantor2MobileNo = guarantor2MobileNo;
	}

	public String getGuarantor2Reminder() {
		return guarantor2Reminder;
	}

	public void setGuarantor2Reminder(String guarantor2Reminder) {
		this.guarantor2Reminder = guarantor2Reminder;
	}
    
}