package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a global freight invoice rule must pass. Check returns its problems, or none. */
interface GlobalFreightInvoiceRuleRule {
    fun check(item: GlobalFreightInvoiceRule): MutableList<String>
}

data class GlobalFreightInvoiceRuleEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalFreightInvoiceRuleRule {
    override fun check(item: GlobalFreightInvoiceRule): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class GlobalFreightInvoiceRuleRequiredCodeRule(var message: String) : GlobalFreightInvoiceRuleRule {
    override fun check(item: GlobalFreightInvoiceRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceRuleAllowedNameRule(
    var allowed: MutableList<String>,
) : GlobalFreightInvoiceRuleRule {
    override fun check(item: GlobalFreightInvoiceRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceRuleCurrencyLengthRule(var maxLength: Int) : GlobalFreightInvoiceRuleRule {
    override fun check(item: GlobalFreightInvoiceRule): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalFreightInvoiceRuleCodePrefixRule(var prefix: String) : GlobalFreightInvoiceRuleRule {
    override fun check(item: GlobalFreightInvoiceRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global freight invoice rule. */
data class GlobalFreightInvoiceRuleValidator(var rules: MutableList<GlobalFreightInvoiceRuleRule>) {
    fun validate(item: GlobalFreightInvoiceRule): MutableList<String> {
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

    fun requireValid(item: GlobalFreightInvoiceRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalFreightInvoiceRule>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalFreightInvoiceRuleValidator(): GlobalFreightInvoiceRuleValidator {
    return GlobalFreightInvoiceRuleValidator(
        mutableListOf<GlobalFreightInvoiceRuleRule>(
            GlobalFreightInvoiceRuleEtaMinutesRangeRule(120, 255),
            GlobalFreightInvoiceRuleRequiredCodeRule("global freight invoice rule needs an entry"),
            GlobalFreightInvoiceRuleAllowedNameRule(mutableListOf<String>("manual", "regional", "adaptive")),
            GlobalFreightInvoiceRuleCurrencyLengthRule(32),
            GlobalFreightInvoiceRuleCodePrefixRule("ACM"),
        ),
    )
}
