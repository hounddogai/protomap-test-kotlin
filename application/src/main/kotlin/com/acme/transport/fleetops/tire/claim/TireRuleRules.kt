package com.acme.transport.fleetops.tire.claim

/** A rule a tire rule must pass. Check returns its problems, or none. */
interface TireRuleRule {
    fun check(item: TireRule): MutableList<String>
}

data class TireRuleRequiredDockRule(var message: String) : TireRuleRule {
    override fun check(item: TireRule): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireRuleCategoryLengthRule(var maxLength: Int) : TireRuleRule {
    override fun check(item: TireRule): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireRuleCapacityRangeRule(var min: Int, var max: Int) : TireRuleRule {
    override fun check(item: TireRule): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire rule. */
data class TireRuleValidator(var rules: MutableList<TireRuleRule>) {
    fun validate(item: TireRule): MutableList<String> {
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

    fun requireValid(item: TireRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTireRuleValidator(): TireRuleValidator {
    return TireRuleValidator(
        mutableListOf<TireRuleRule>(
            TireRuleRequiredDockRule("tire rule needs this field"),
            TireRuleCategoryLengthRule(80),
            TireRuleCapacityRangeRule(16, 1000),
        ),
    )
}
