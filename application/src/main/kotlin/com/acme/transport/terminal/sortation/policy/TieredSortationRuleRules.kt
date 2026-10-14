package com.acme.transport.terminal.sortation.policy

/** A rule a tiered sortation rule must pass. Check returns its problems, or none. */
interface TieredSortationRuleRule {
    fun check(item: TieredSortationRule): MutableList<String>
}

data class TieredSortationRuleSequenceRangeRule(var min: Int, var max: Int) : TieredSortationRuleRule {
    override fun check(item: TieredSortationRule): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class TieredSortationRuleCodeLengthRule(var maxLength: Int) : TieredSortationRuleRule {
    override fun check(item: TieredSortationRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationRuleRequiredCodeRule(var message: String) : TieredSortationRuleRule {
    override fun check(item: TieredSortationRule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationRuleTitleLengthRule(var maxLength: Int) : TieredSortationRuleRule {
    override fun check(item: TieredSortationRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered sortation rule. */
data class TieredSortationRuleValidator(var rules: MutableList<TieredSortationRuleRule>) {
    fun validate(item: TieredSortationRule): MutableList<String> {
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

    fun requireValid(item: TieredSortationRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationRule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationRuleValidator(): TieredSortationRuleValidator {
    return TieredSortationRuleValidator(
        mutableListOf<TieredSortationRuleRule>(
            TieredSortationRuleSequenceRangeRule(255, 86400),
            TieredSortationRuleCodeLengthRule(16),
            TieredSortationRuleRequiredCodeRule("tiered sortation rule needs this field"),
            TieredSortationRuleTitleLengthRule(1000),
        ),
    )
}
