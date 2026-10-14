package com.acme.transport.emissions.routeoffset.offsetbaseline

/** A rule a scheduled route offset allocation must pass. Check returns its problems, or none. */
interface ScheduledRouteOffsetAllocationRule {
    fun check(item: ScheduledRouteOffsetAllocation): MutableList<String>
}

data class ScheduledRouteOffsetAllocationEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRouteOffsetAllocationRule {
    override fun check(item: ScheduledRouteOffsetAllocation): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetAllocationChannelLengthRule(
    var maxLength: Int,
) : ScheduledRouteOffsetAllocationRule {
    override fun check(item: ScheduledRouteOffsetAllocation): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetAllocationRequiredChannelRule(
    var message: String,
) : ScheduledRouteOffsetAllocationRule {
    override fun check(item: ScheduledRouteOffsetAllocation): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledRouteOffsetAllocationUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledRouteOffsetAllocationRule {
    override fun check(item: ScheduledRouteOffsetAllocation): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled route offset allocation. */
data class ScheduledRouteOffsetAllocationValidator(var rules: MutableList<ScheduledRouteOffsetAllocationRule>) {
    fun validate(item: ScheduledRouteOffsetAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledRouteOffsetAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledRouteOffsetAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledRouteOffsetAllocationValidator(): ScheduledRouteOffsetAllocationValidator {
    return ScheduledRouteOffsetAllocationValidator(
        mutableListOf<ScheduledRouteOffsetAllocationRule>(
            ScheduledRouteOffsetAllocationEtaMinutesRangeRule(64, 1000),
            ScheduledRouteOffsetAllocationChannelLengthRule(64),
            ScheduledRouteOffsetAllocationRequiredChannelRule(
                "scheduled route offset allocation needs an entry",
            ),
            ScheduledRouteOffsetAllocationUpdatedAtRangeRule(80, 80),
        ),
    )
}
