package com.acme.transport.emissions.fuelburn.milestone

/** A rule a fallback fuel burn milestone must pass. Check returns its problems, or none. */
interface FallbackFuelBurnMilestoneRule {
    fun check(item: FallbackFuelBurnMilestone): MutableList<String>
}

data class FallbackFuelBurnMilestoneLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackFuelBurnMilestoneRule {
    override fun check(item: FallbackFuelBurnMilestone): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FallbackFuelBurnMilestoneStopCountRangeRule(
    var min: Int,
    var max: Int,
) : FallbackFuelBurnMilestoneRule {
    override fun check(item: FallbackFuelBurnMilestone): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackFuelBurnMilestoneTitleLengthRule(var maxLength: Int) : FallbackFuelBurnMilestoneRule {
    override fun check(item: FallbackFuelBurnMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback fuel burn milestone. */
data class FallbackFuelBurnMilestoneValidator(var rules: MutableList<FallbackFuelBurnMilestoneRule>) {
    fun validate(item: FallbackFuelBurnMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackFuelBurnMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackFuelBurnMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackFuelBurnMilestoneValidator(): FallbackFuelBurnMilestoneValidator {
    return FallbackFuelBurnMilestoneValidator(
        mutableListOf<FallbackFuelBurnMilestoneRule>(
            FallbackFuelBurnMilestoneLoadKgRangeRule(7.1, 2074.6),
            FallbackFuelBurnMilestoneStopCountRangeRule(64, 80),
            FallbackFuelBurnMilestoneTitleLengthRule(64),
        ),
    )
}
