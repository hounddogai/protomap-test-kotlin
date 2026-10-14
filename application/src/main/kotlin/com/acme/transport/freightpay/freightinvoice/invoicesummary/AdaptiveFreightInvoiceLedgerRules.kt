package com.acme.transport.freightpay.freightinvoice.invoicesummary

/** A rule a adaptive freight invoice ledger must pass. Check returns its problems, or none. */
interface AdaptiveFreightInvoiceLedgerRule {
    fun check(item: AdaptiveFreightInvoiceLedger): MutableList<String>
}

data class AdaptiveFreightInvoiceLedgerApprovedGuardRule(var limit: Double) : AdaptiveFreightInvoiceLedgerRule {
    override fun check(item: AdaptiveFreightInvoiceLedger): MutableList<String> {
        if (item.approved && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceLedgerTitleLengthRule(var maxLength: Int) : AdaptiveFreightInvoiceLedgerRule {
    override fun check(item: AdaptiveFreightInvoiceLedger): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceLedgerRequiredContactEmailRule(
    var message: String,
) : AdaptiveFreightInvoiceLedgerRule {
    override fun check(item: AdaptiveFreightInvoiceLedger): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceLedgerContactEmailLengthRule(
    var maxLength: Int,
) : AdaptiveFreightInvoiceLedgerRule {
    override fun check(item: AdaptiveFreightInvoiceLedger): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFreightInvoiceLedgerRequiredCodeRule(
    var message: String,
) : AdaptiveFreightInvoiceLedgerRule {
    override fun check(item: AdaptiveFreightInvoiceLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive freight invoice ledger. */
data class AdaptiveFreightInvoiceLedgerValidator(var rules: MutableList<AdaptiveFreightInvoiceLedgerRule>) {
    fun validate(item: AdaptiveFreightInvoiceLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveFreightInvoiceLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveFreightInvoiceLedger>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultAdaptiveFreightInvoiceLedgerValidator(): AdaptiveFreightInvoiceLedgerValidator {
    return AdaptiveFreightInvoiceLedgerValidator(
        mutableListOf<AdaptiveFreightInvoiceLedgerRule>(
            AdaptiveFreightInvoiceLedgerApprovedGuardRule(9.4),
            AdaptiveFreightInvoiceLedgerTitleLengthRule(120),
            AdaptiveFreightInvoiceLedgerRequiredContactEmailRule(
                "adaptive freight invoice ledger needs this field",
            ),
            AdaptiveFreightInvoiceLedgerContactEmailLengthRule(86400),
            AdaptiveFreightInvoiceLedgerRequiredCodeRule("adaptive freight invoice ledger needs a value"),
        ),
    )
}
