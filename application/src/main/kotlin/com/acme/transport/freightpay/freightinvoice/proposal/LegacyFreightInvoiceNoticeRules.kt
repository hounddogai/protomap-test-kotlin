package com.acme.transport.freightpay.freightinvoice.proposal

/** A rule a legacy freight invoice notice must pass. Check returns its problems, or none. */
interface LegacyFreightInvoiceNoticeRule {
    fun check(item: LegacyFreightInvoiceNotice): MutableList<String>
}

data class LegacyFreightInvoiceNoticeRetriesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyFreightInvoiceNoticeRule {
    override fun check(item: LegacyFreightInvoiceNotice): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class LegacyFreightInvoiceNoticeOdometerRangeRule(
    var min: Int,
    var max: Int,
) : LegacyFreightInvoiceNoticeRule {
    override fun check(item: LegacyFreightInvoiceNotice): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceNoticeExternalRefLengthRule(
    var maxLength: Int,
) : LegacyFreightInvoiceNoticeRule {
    override fun check(item: LegacyFreightInvoiceNotice): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceNoticeRequiredTitleRule(var message: String) : LegacyFreightInvoiceNoticeRule {
    override fun check(item: LegacyFreightInvoiceNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy freight invoice notice. */
data class LegacyFreightInvoiceNoticeValidator(var rules: MutableList<LegacyFreightInvoiceNoticeRule>) {
    fun validate(item: LegacyFreightInvoiceNotice): MutableList<String> {
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

    fun requireValid(item: LegacyFreightInvoiceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyFreightInvoiceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyFreightInvoiceNoticeValidator(): LegacyFreightInvoiceNoticeValidator {
    return LegacyFreightInvoiceNoticeValidator(
        mutableListOf<LegacyFreightInvoiceNoticeRule>(
            LegacyFreightInvoiceNoticeRetriesRangeRule(16, 16),
            LegacyFreightInvoiceNoticeOdometerRangeRule(32, 255),
            LegacyFreightInvoiceNoticeExternalRefLengthRule(1000),
            LegacyFreightInvoiceNoticeRequiredTitleRule("legacy freight invoice notice needs an entry"),
        ),
    )
}
