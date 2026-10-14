package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.kit.text.normalize

/** A rule a default fuel surcharge rule must pass. Check returns its problems, or none. */
interface DefaultFuelSurchargeRuleRule {
    fun check(item: DefaultFuelSurchargeRule): MutableList<String>
}

data class DefaultFuelSurchargeRuleRequiredCountryRule(var message: String) : DefaultFuelSurchargeRuleRule {
    override fun check(item: DefaultFuelSurchargeRule): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeRuleRequiredCodeRule(var message: String) : DefaultFuelSurchargeRuleRule {
    override fun check(item: DefaultFuelSurchargeRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeRuleAllowedReviewerRule(
    var allowed: MutableList<String>,
) : DefaultFuelSurchargeRuleRule {
    override fun check(item: DefaultFuelSurchargeRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeRuleVisibleGuardRule(var limit: Int) : DefaultFuelSurchargeRuleRule {
    override fun check(item: DefaultFuelSurchargeRule): MutableList<String> {
        if (item.visible && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DefaultFuelSurchargeRuleCodePrefixRule(var prefix: String) : DefaultFuelSurchargeRuleRule {
    override fun check(item: DefaultFuelSurchargeRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default fuel surcharge rule. */
data class DefaultFuelSurchargeRuleValidator(var rules: MutableList<DefaultFuelSurchargeRuleRule>) {
    fun validate(item: DefaultFuelSurchargeRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultFuelSurchargeRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultFuelSurchargeRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultFuelSurchargeRuleValidator(): DefaultFuelSurchargeRuleValidator {
    return DefaultFuelSurchargeRuleValidator(
        mutableListOf<DefaultFuelSurchargeRuleRule>(
            DefaultFuelSurchargeRuleRequiredCountryRule("default fuel surcharge rule needs this field"),
            DefaultFuelSurchargeRuleRequiredCodeRule("default fuel surcharge rule needs a value"),
            DefaultFuelSurchargeRuleAllowedReviewerRule(mutableListOf<String>()),
            DefaultFuelSurchargeRuleVisibleGuardRule(64),
            DefaultFuelSurchargeRuleCodePrefixRule("X"),
        ),
    )
}
