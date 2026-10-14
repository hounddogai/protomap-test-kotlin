package com.acme.transport.customs.clearance.milestone

/** A rule a seasonal clearance milestone must pass. Check returns its problems, or none. */
interface SeasonalClearanceMilestoneRule {
    fun check(item: SeasonalClearanceMilestone): MutableList<String>
}

data class SeasonalClearanceMilestoneCodePrefixRule(var prefix: String) : SeasonalClearanceMilestoneRule {
    override fun check(item: SeasonalClearanceMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceMilestoneCapacityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalClearanceMilestoneRule {
    override fun check(item: SeasonalClearanceMilestone): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class SeasonalClearanceMilestoneSourceLengthRule(var maxLength: Int) : SeasonalClearanceMilestoneRule {
    override fun check(item: SeasonalClearanceMilestone): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalClearanceMilestoneBudgetRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalClearanceMilestoneRule {
    override fun check(item: SeasonalClearanceMilestone): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal clearance milestone. */
data class SeasonalClearanceMilestoneValidator(var rules: MutableList<SeasonalClearanceMilestoneRule>) {
    fun validate(item: SeasonalClearanceMilestone): MutableList<String> {
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

    fun requireValid(item: SeasonalClearanceMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalClearanceMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalClearanceMilestoneValidator(): SeasonalClearanceMilestoneValidator {
    return SeasonalClearanceMilestoneValidator(
        mutableListOf<SeasonalClearanceMilestoneRule>(
            SeasonalClearanceMilestoneCodePrefixRule("ACM"),
            SeasonalClearanceMilestoneCapacityRangeRule(80, 1000),
            SeasonalClearanceMilestoneSourceLengthRule(86400),
            SeasonalClearanceMilestoneBudgetRangeRule(2.8, 4984.0),
        ),
    )
}
