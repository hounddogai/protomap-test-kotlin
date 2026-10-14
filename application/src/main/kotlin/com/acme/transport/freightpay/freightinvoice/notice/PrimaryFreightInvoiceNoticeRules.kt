package com.acme.transport.freightpay.freightinvoice.notice

/** A rule a primary freight invoice notice must pass. Check returns its problems, or none. */
interface PrimaryFreightInvoiceNoticeRule {
    fun check(item: PrimaryFreightInvoiceNotice): MutableList<String>
}

data class PrimaryFreightInvoiceNoticeLabelLengthRule(var maxLength: Int) : PrimaryFreightInvoiceNoticeRule {
    override fun check(item: PrimaryFreightInvoiceNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceNoticeDescriptionLengthRule(
    var maxLength: Int,
) : PrimaryFreightInvoiceNoticeRule {
    override fun check(item: PrimaryFreightInvoiceNotice): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryFreightInvoiceNoticeRequiredDescriptionRule(
    var message: String,
) : PrimaryFreightInvoiceNoticeRule {
    override fun check(item: PrimaryFreightInvoiceNotice): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary freight invoice notice. */
data class PrimaryFreightInvoiceNoticeValidator(var rules: MutableList<PrimaryFreightInvoiceNoticeRule>) {
    fun validate(item: PrimaryFreightInvoiceNotice): MutableList<String> {
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

    fun requireValid(item: PrimaryFreightInvoiceNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryFreightInvoiceNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryFreightInvoiceNoticeValidator(): PrimaryFreightInvoiceNoticeValidator {
    return PrimaryFreightInvoiceNoticeValidator(
        mutableListOf<PrimaryFreightInvoiceNoticeRule>(
            PrimaryFreightInvoiceNoticeLabelLengthRule(120),
            PrimaryFreightInvoiceNoticeDescriptionLengthRule(80),
            PrimaryFreightInvoiceNoticeRequiredDescriptionRule("primary freight invoice notice needs an entry"),
        ),
    )
}
