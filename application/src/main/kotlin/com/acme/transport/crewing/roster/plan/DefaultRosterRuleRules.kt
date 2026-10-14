package com.acme.transport.crewing.roster.plan

/** A rule a default roster rule must pass. Check returns its problems, or none. */
interface DefaultRosterRuleRule {
    fun check(item: DefaultRosterRule): MutableList<String>
}

data class DefaultRosterRuleCodePrefixRule(var prefix: String) : DefaultRosterRuleRule {
    override fun check(item: DefaultRosterRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterRuleBudgetRangeRule(var min: Double, var max: Double) : DefaultRosterRuleRule {
    override fun check(item: DefaultRosterRule): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRosterRuleRequiredTitleRule(var message: String) : DefaultRosterRuleRule {
    override fun check(item: DefaultRosterRule): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default roster rule. */
data class DefaultRosterRuleValidator(var rules: MutableList<DefaultRosterRuleRule>) {
    fun validate(item: DefaultRosterRule): MutableList<String> {
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

    fun requireValid(item: DefaultRosterRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRosterRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRosterRuleValidator(): DefaultRosterRuleValidator {
    return DefaultRosterRuleValidator(
        mutableListOf<DefaultRosterRuleRule>(
            DefaultRosterRuleCodePrefixRule("X"),
            DefaultRosterRuleBudgetRangeRule(3.5, 392.3),
            DefaultRosterRuleRequiredTitleRule("default roster rule needs a value"),
        ),
    )
}
