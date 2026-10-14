package com.acme.transport.terminal.dockdoor.metric

/** A rule a dock door milestone must pass. Check returns its problems, or none. */
interface DockDoorMilestoneRule {
    fun check(item: DockDoorMilestone): MutableList<String>
}

data class DockDoorMilestoneWeightRangeRule(var min: Double, var max: Double) : DockDoorMilestoneRule {
    override fun check(item: DockDoorMilestone): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class DockDoorMilestoneDiscountRangeRule(var min: Double, var max: Double) : DockDoorMilestoneRule {
    override fun check(item: DockDoorMilestone): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class DockDoorMilestoneTitleLengthRule(var maxLength: Int) : DockDoorMilestoneRule {
    override fun check(item: DockDoorMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorMilestoneRatioRangeRule(var min: Double, var max: Double) : DockDoorMilestoneRule {
    override fun check(item: DockDoorMilestone): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door milestone. */
data class DockDoorMilestoneValidator(var rules: MutableList<DockDoorMilestoneRule>) {
    fun validate(item: DockDoorMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorMilestoneValidator(): DockDoorMilestoneValidator {
    return DockDoorMilestoneValidator(
        mutableListOf<DockDoorMilestoneRule>(
            DockDoorMilestoneWeightRangeRule(1.0, 4027.8),
            DockDoorMilestoneDiscountRangeRule(1.5, 897.3),
            DockDoorMilestoneTitleLengthRule(86400),
            DockDoorMilestoneRatioRangeRule(7.6, 4696.1),
        ),
    )
}
