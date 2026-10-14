package com.acme.transport.terminal.sortation.variance

/** A rule a default sortation rule must pass. Check returns its problems, or none. */
interface DefaultSortationRuleRule {
    fun check(item: DefaultSortationRule): MutableList<String>
}

data class DefaultSortationRuleRequiredCodeRule(var message: String) : DefaultSortationRuleRule {
    override fun check(item: DefaultSortationRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRuleRequiredNameRule(var message: String) : DefaultSortationRuleRule {
    override fun check(item: DefaultSortationRule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRuleApprovedGuardRule(var limit: Double) : DefaultSortationRuleRule {
    override fun check(item: DefaultSortationRule): MutableList<String> {
        if (item.approved && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSortationRuleBudgetRangeRule(var min: Double, var max: Double) : DefaultSortationRuleRule {
    override fun check(item: DefaultSortationRule): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default sortation rule. */
data class DefaultSortationRuleValidator(var rules: MutableList<DefaultSortationRuleRule>) {
    fun validate(item: DefaultSortationRule): MutableList<String> {
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

    fun requireValid(item: DefaultSortationRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultSortationRule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultSortationRuleValidator(): DefaultSortationRuleValidator {
    return DefaultSortationRuleValidator(
        mutableListOf<DefaultSortationRuleRule>(
            DefaultSortationRuleRequiredCodeRule("default sortation rule needs a value"),
            DefaultSortationRuleRequiredNameRule("default sortation rule needs this field"),
            DefaultSortationRuleApprovedGuardRule(6.2),
            DefaultSortationRuleBudgetRangeRule(7.1, 4135.5),
        ),
    )
}
