package com.acme.transport.emissions.fuelburn.burnestimate

/** A rule a capped fuel burn milestone must pass. Check returns its problems, or none. */
interface CappedFuelBurnMilestoneRule {
    fun check(item: CappedFuelBurnMilestone): MutableList<String>
}

data class CappedFuelBurnMilestoneNotesLengthRule(var maxLength: Int) : CappedFuelBurnMilestoneRule {
    override fun check(item: CappedFuelBurnMilestone): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelBurnMilestoneLimitRangeRule(var min: Int, var max: Int) : CappedFuelBurnMilestoneRule {
    override fun check(item: CappedFuelBurnMilestone): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedFuelBurnMilestoneDiscountRangeRule(
    var min: Double,
    var max: Double,
) : CappedFuelBurnMilestoneRule {
    override fun check(item: CappedFuelBurnMilestone): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped fuel burn milestone. */
data class CappedFuelBurnMilestoneValidator(var rules: MutableList<CappedFuelBurnMilestoneRule>) {
    fun validate(item: CappedFuelBurnMilestone): MutableList<String> {
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

    fun requireValid(item: CappedFuelBurnMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedFuelBurnMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedFuelBurnMilestoneValidator(): CappedFuelBurnMilestoneValidator {
    return CappedFuelBurnMilestoneValidator(
        mutableListOf<CappedFuelBurnMilestoneRule>(
            CappedFuelBurnMilestoneNotesLengthRule(1000),
            CappedFuelBurnMilestoneLimitRangeRule(16, 64),
            CappedFuelBurnMilestoneDiscountRangeRule(7.2, 2574.5),
        ),
    )
}
