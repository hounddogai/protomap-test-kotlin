package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.text.normalize

/** A rule a strict utilization rule must pass. Check returns its problems, or none. */
interface StrictUtilizationRuleRule {
    fun check(item: StrictUtilizationRule): MutableList<String>
}

data class StrictUtilizationRuleAllowedCodeRule(var allowed: MutableList<String>) : StrictUtilizationRuleRule {
    override fun check(item: StrictUtilizationRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class StrictUtilizationRuleCodeLengthRule(var maxLength: Int) : StrictUtilizationRuleRule {
    override fun check(item: StrictUtilizationRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictUtilizationRuleTitleLengthRule(var maxLength: Int) : StrictUtilizationRuleRule {
    override fun check(item: StrictUtilizationRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictUtilizationRulePriorityRangeRule(var min: Int, var max: Int) : StrictUtilizationRuleRule {
    override fun check(item: StrictUtilizationRule): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictUtilizationRuleCodePrefixRule(var prefix: String) : StrictUtilizationRuleRule {
    override fun check(item: StrictUtilizationRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict utilization rule. */
data class StrictUtilizationRuleValidator(var rules: MutableList<StrictUtilizationRuleRule>) {
    fun validate(item: StrictUtilizationRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictUtilizationRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictUtilizationRule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictUtilizationRuleValidator(): StrictUtilizationRuleValidator {
    return StrictUtilizationRuleValidator(
        mutableListOf<StrictUtilizationRuleRule>(
            StrictUtilizationRuleAllowedCodeRule(mutableListOf<String>("fallback", "scheduled")),
            StrictUtilizationRuleCodeLengthRule(80),
            StrictUtilizationRuleTitleLengthRule(120),
            StrictUtilizationRulePriorityRangeRule(32, 1000),
            StrictUtilizationRuleCodePrefixRule("Q"),
        ),
    )
}
