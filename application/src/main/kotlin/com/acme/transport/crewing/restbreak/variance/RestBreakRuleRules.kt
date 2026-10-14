package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.text.normalize

/** A rule a rest break rule must pass. Check returns its problems, or none. */
interface RestBreakRuleRule {
    fun check(item: RestBreakRule): MutableList<String>
}

data class RestBreakRuleNameLengthRule(var maxLength: Int) : RestBreakRuleRule {
    override fun check(item: RestBreakRule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRuleCostRangeRule(var min: Double, var max: Double) : RestBreakRuleRule {
    override fun check(item: RestBreakRule): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRuleRequiredCodeRule(var message: String) : RestBreakRuleRule {
    override fun check(item: RestBreakRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRuleCodePrefixRule(var prefix: String) : RestBreakRuleRule {
    override fun check(item: RestBreakRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRuleAllowedNameRule(var allowed: MutableList<String>) : RestBreakRuleRule {
    override fun check(item: RestBreakRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRuleThresholdRangeRule(var min: Double, var max: Double) : RestBreakRuleRule {
    override fun check(item: RestBreakRule): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break rule. */
data class RestBreakRuleValidator(var rules: MutableList<RestBreakRuleRule>) {
    fun validate(item: RestBreakRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakRuleValidator(): RestBreakRuleValidator {
    return RestBreakRuleValidator(
        mutableListOf<RestBreakRuleRule>(
            RestBreakRuleNameLengthRule(32),
            RestBreakRuleCostRangeRule(5.2, 530.6),
            RestBreakRuleRequiredCodeRule("rest break rule needs a value"),
            RestBreakRuleCodePrefixRule("REF"),
            RestBreakRuleAllowedNameRule(mutableListOf<String>()),
            RestBreakRuleThresholdRangeRule(3.3, 1061.9),
        ),
    )
}
