package com.acme.transport.freightpay.freightinvoice.baseline

/** A rule a legacy freight invoice rule must pass. Check returns its problems, or none. */
interface LegacyFreightInvoiceRuleRule {
    fun check(item: LegacyFreightInvoiceRule): MutableList<String>
}

data class LegacyFreightInvoiceRuleNameLengthRule(var maxLength: Int) : LegacyFreightInvoiceRuleRule {
    override fun check(item: LegacyFreightInvoiceRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceRuleTitleLengthRule(var maxLength: Int) : LegacyFreightInvoiceRuleRule {
    override fun check(item: LegacyFreightInvoiceRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceRuleLimitRangeRule(var min: Int, var max: Int) : LegacyFreightInvoiceRuleRule {
    override fun check(item: LegacyFreightInvoiceRule): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyFreightInvoiceRuleCodeLengthRule(var maxLength: Int) : LegacyFreightInvoiceRuleRule {
    override fun check(item: LegacyFreightInvoiceRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy freight invoice rule. */
data class LegacyFreightInvoiceRuleValidator(var rules: MutableList<LegacyFreightInvoiceRuleRule>) {
    fun validate(item: LegacyFreightInvoiceRule): MutableList<String> {
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

    fun requireValid(item: LegacyFreightInvoiceRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyFreightInvoiceRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyFreightInvoiceRuleValidator(): LegacyFreightInvoiceRuleValidator {
    return LegacyFreightInvoiceRuleValidator(
        mutableListOf<LegacyFreightInvoiceRuleRule>(
            LegacyFreightInvoiceRuleNameLengthRule(64),
            LegacyFreightInvoiceRuleTitleLengthRule(120),
            LegacyFreightInvoiceRuleLimitRangeRule(16, 86400),
            LegacyFreightInvoiceRuleCodeLengthRule(255),
        ),
    )
}
