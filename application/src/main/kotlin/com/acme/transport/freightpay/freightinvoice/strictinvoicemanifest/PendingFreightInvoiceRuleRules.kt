package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

/** A rule a pending freight invoice rule must pass. Check returns its problems, or none. */
interface PendingFreightInvoiceRuleRule {
    fun check(item: PendingFreightInvoiceRule): MutableList<String>
}

data class PendingFreightInvoiceRuleTitleLengthRule(var maxLength: Int) : PendingFreightInvoiceRuleRule {
    override fun check(item: PendingFreightInvoiceRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingFreightInvoiceRuleRatioRangeRule(
    var min: Double,
    var max: Double,
) : PendingFreightInvoiceRuleRule {
    override fun check(item: PendingFreightInvoiceRule): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PendingFreightInvoiceRuleRequiredTitleRule(var message: String) : PendingFreightInvoiceRuleRule {
    override fun check(item: PendingFreightInvoiceRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending freight invoice rule. */
data class PendingFreightInvoiceRuleValidator(var rules: MutableList<PendingFreightInvoiceRuleRule>) {
    fun validate(item: PendingFreightInvoiceRule): MutableList<String> {
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

    fun requireValid(item: PendingFreightInvoiceRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingFreightInvoiceRule>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingFreightInvoiceRuleValidator(): PendingFreightInvoiceRuleValidator {
    return PendingFreightInvoiceRuleValidator(
        mutableListOf<PendingFreightInvoiceRuleRule>(
            PendingFreightInvoiceRuleTitleLengthRule(1000),
            PendingFreightInvoiceRuleRatioRangeRule(0.7, 1315.4),
            PendingFreightInvoiceRuleRequiredTitleRule("pending freight invoice rule needs a value"),
        ),
    )
}
