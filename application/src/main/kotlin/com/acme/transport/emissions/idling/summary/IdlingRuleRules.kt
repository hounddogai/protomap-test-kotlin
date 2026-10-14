package com.acme.transport.emissions.idling.summary

/** A rule a idling rule must pass. Check returns its problems, or none. */
interface IdlingRuleRule {
    fun check(item: IdlingRule): MutableList<String>
}

data class IdlingRuleThresholdRangeRule(var min: Double, var max: Double) : IdlingRuleRule {
    override fun check(item: IdlingRule): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRuleTitleLengthRule(var maxLength: Int) : IdlingRuleRule {
    override fun check(item: IdlingRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRuleRequiredCodeRule(var message: String) : IdlingRuleRule {
    override fun check(item: IdlingRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingRuleCodePrefixRule(var prefix: String) : IdlingRuleRule {
    override fun check(item: IdlingRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRuleCodeLengthRule(var maxLength: Int) : IdlingRuleRule {
    override fun check(item: IdlingRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingRuleDepotLengthRule(var maxLength: Int) : IdlingRuleRule {
    override fun check(item: IdlingRule): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling rule. */
data class IdlingRuleValidator(var rules: MutableList<IdlingRuleRule>) {
    fun validate(item: IdlingRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingRule>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingRuleValidator(): IdlingRuleValidator {
    return IdlingRuleValidator(
        mutableListOf<IdlingRuleRule>(
            IdlingRuleThresholdRangeRule(1.4, 3595.4),
            IdlingRuleTitleLengthRule(64),
            IdlingRuleRequiredCodeRule("idling rule needs this field"),
            IdlingRuleCodePrefixRule("ACM"),
            IdlingRuleCodeLengthRule(16),
            IdlingRuleDepotLengthRule(1000),
        ),
    )
}
