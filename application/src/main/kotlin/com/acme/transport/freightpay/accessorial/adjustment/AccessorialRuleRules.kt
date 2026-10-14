package com.acme.transport.freightpay.accessorial.adjustment

/** A rule a accessorial rule must pass. Check returns its problems, or none. */
interface AccessorialRuleRule {
    fun check(item: AccessorialRule): MutableList<String>
}

data class AccessorialRuleCurrencyLengthRule(var maxLength: Int) : AccessorialRuleRule {
    override fun check(item: AccessorialRule): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRuleDurationMinutesRangeRule(var min: Int, var max: Int) : AccessorialRuleRule {
    override fun check(item: AccessorialRule): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRuleRequiredCurrencyRule(var message: String) : AccessorialRuleRule {
    override fun check(item: AccessorialRule): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRuleCodePrefixRule(var prefix: String) : AccessorialRuleRule {
    override fun check(item: AccessorialRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRuleRequiredCategoryRule(var message: String) : AccessorialRuleRule {
    override fun check(item: AccessorialRule): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialRuleCategoryLengthRule(var maxLength: Int) : AccessorialRuleRule {
    override fun check(item: AccessorialRule): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial rule. */
data class AccessorialRuleValidator(var rules: MutableList<AccessorialRuleRule>) {
    fun validate(item: AccessorialRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialRule>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialRuleValidator(): AccessorialRuleValidator {
    return AccessorialRuleValidator(
        mutableListOf<AccessorialRuleRule>(
            AccessorialRuleCurrencyLengthRule(255),
            AccessorialRuleDurationMinutesRangeRule(80, 255),
            AccessorialRuleRequiredCurrencyRule("accessorial rule needs this field"),
            AccessorialRuleCodePrefixRule("Q"),
            AccessorialRuleRequiredCategoryRule("accessorial rule needs a value"),
            AccessorialRuleCategoryLengthRule(1000),
        ),
    )
}
