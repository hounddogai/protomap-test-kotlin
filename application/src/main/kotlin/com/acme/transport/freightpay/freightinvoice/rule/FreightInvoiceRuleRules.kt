package com.acme.transport.freightpay.freightinvoice.rule

/** A rule a freight invoice rule must pass. Check returns its problems, or none. */
interface FreightInvoiceRuleRule {
    fun check(item: FreightInvoiceRule): MutableList<String>
}

data class FreightInvoiceRuleNameLengthRule(var maxLength: Int) : FreightInvoiceRuleRule {
    override fun check(item: FreightInvoiceRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRuleRequiredTitleRule(var message: String) : FreightInvoiceRuleRule {
    override fun check(item: FreightInvoiceRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRuleTitleLengthRule(var maxLength: Int) : FreightInvoiceRuleRule {
    override fun check(item: FreightInvoiceRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRuleQuantityRangeRule(var min: Int, var max: Int) : FreightInvoiceRuleRule {
    override fun check(item: FreightInvoiceRule): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FreightInvoiceRuleRequiredDriverIdRule(var message: String) : FreightInvoiceRuleRule {
    override fun check(item: FreightInvoiceRule): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a freight invoice rule. */
data class FreightInvoiceRuleValidator(var rules: MutableList<FreightInvoiceRuleRule>) {
    fun validate(item: FreightInvoiceRule): MutableList<String> {
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

    fun requireValid(item: FreightInvoiceRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FreightInvoiceRule>): MutableMap<String, MutableList<String>> {
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

fun defaultFreightInvoiceRuleValidator(): FreightInvoiceRuleValidator {
    return FreightInvoiceRuleValidator(
        mutableListOf<FreightInvoiceRuleRule>(
            FreightInvoiceRuleNameLengthRule(3600),
            FreightInvoiceRuleRequiredTitleRule("freight invoice rule needs a value"),
            FreightInvoiceRuleTitleLengthRule(3600),
            FreightInvoiceRuleQuantityRangeRule(120, 120),
            FreightInvoiceRuleRequiredDriverIdRule("freight invoice rule needs this field"),
        ),
    )
}
