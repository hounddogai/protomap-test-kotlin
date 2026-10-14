package com.acme.transport.network.node.milestone

/** A rule a scheduled node milestone must pass. Check returns its problems, or none. */
interface ScheduledNodeMilestoneRule {
    fun check(item: ScheduledNodeMilestone): MutableList<String>
}

data class ScheduledNodeMilestoneRequiredCodeRule(var message: String) : ScheduledNodeMilestoneRule {
    override fun check(item: ScheduledNodeMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNodeMilestoneRequiredVehicleIdRule(var message: String) : ScheduledNodeMilestoneRule {
    override fun check(item: ScheduledNodeMilestone): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNodeMilestoneRatioRangeRule(var min: Double, var max: Double) : ScheduledNodeMilestoneRule {
    override fun check(item: ScheduledNodeMilestone): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNodeMilestoneDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledNodeMilestoneRule {
    override fun check(item: ScheduledNodeMilestone): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNodeMilestoneCodePrefixRule(var prefix: String) : ScheduledNodeMilestoneRule {
    override fun check(item: ScheduledNodeMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNodeMilestoneTrailerIdLengthRule(var maxLength: Int) : ScheduledNodeMilestoneRule {
    override fun check(item: ScheduledNodeMilestone): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled node milestone. */
data class ScheduledNodeMilestoneValidator(var rules: MutableList<ScheduledNodeMilestoneRule>) {
    fun validate(item: ScheduledNodeMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledNodeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNodeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNodeMilestoneValidator(): ScheduledNodeMilestoneValidator {
    return ScheduledNodeMilestoneValidator(
        mutableListOf<ScheduledNodeMilestoneRule>(
            ScheduledNodeMilestoneRequiredCodeRule("scheduled node milestone needs this field"),
            ScheduledNodeMilestoneRequiredVehicleIdRule("scheduled node milestone needs an entry"),
            ScheduledNodeMilestoneRatioRangeRule(2.6, 4916.6),
            ScheduledNodeMilestoneDurationMinutesRangeRule(32, 64),
            ScheduledNodeMilestoneCodePrefixRule("X"),
            ScheduledNodeMilestoneTrailerIdLengthRule(16),
        ),
    )
}
