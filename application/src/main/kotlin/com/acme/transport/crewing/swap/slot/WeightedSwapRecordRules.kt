package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.text.normalize

/** A rule a weighted swap record must pass. Check returns its problems, or none. */
interface WeightedSwapRecordRule {
    fun check(item: WeightedSwapRecord): MutableList<String>
}

data class WeightedSwapRecordCodePrefixRule(var prefix: String) : WeightedSwapRecordRule {
    override fun check(item: WeightedSwapRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapRecordRequiredLabelRule(var message: String) : WeightedSwapRecordRule {
    override fun check(item: WeightedSwapRecord): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapRecordAmountRangeRule(var min: Double, var max: Double) : WeightedSwapRecordRule {
    override fun check(item: WeightedSwapRecord): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapRecordAllowedCategoryRule(var allowed: MutableList<String>) : WeightedSwapRecordRule {
    override fun check(item: WeightedSwapRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapRecordArchivedGuardRule(var limit: Double) : WeightedSwapRecordRule {
    override fun check(item: WeightedSwapRecord): MutableList<String> {
        if (item.archived && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted swap record. */
data class WeightedSwapRecordValidator(var rules: MutableList<WeightedSwapRecordRule>) {
    fun validate(item: WeightedSwapRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedSwapRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedSwapRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedSwapRecordValidator(): WeightedSwapRecordValidator {
    return WeightedSwapRecordValidator(
        mutableListOf<WeightedSwapRecordRule>(
            WeightedSwapRecordCodePrefixRule("Q"),
            WeightedSwapRecordRequiredLabelRule("weighted swap record needs an entry"),
            WeightedSwapRecordAmountRangeRule(8.9, 1321.2),
            WeightedSwapRecordAllowedCategoryRule(mutableListOf<String>("default")),
            WeightedSwapRecordArchivedGuardRule(5.3),
        ),
    )
}
