package com.acme.transport.crewing.availability.defaultavailabilitysegment

/** A rule a scheduled availability segment must pass. Check returns its problems, or none. */
interface ScheduledAvailabilitySegmentRule {
    fun check(item: ScheduledAvailabilitySegment): MutableList<String>
}

data class ScheduledAvailabilitySegmentRequiredCodeRule(
    var message: String,
) : ScheduledAvailabilitySegmentRule {
    override fun check(item: ScheduledAvailabilitySegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilitySegmentWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAvailabilitySegmentRule {
    override fun check(item: ScheduledAvailabilitySegment): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class ScheduledAvailabilitySegmentRequiredDriverIdRule(
    var message: String,
) : ScheduledAvailabilitySegmentRule {
    override fun check(item: ScheduledAvailabilitySegment): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilitySegmentRequiredLabelRule(
    var message: String,
) : ScheduledAvailabilitySegmentRule {
    override fun check(item: ScheduledAvailabilitySegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled availability segment. */
data class ScheduledAvailabilitySegmentValidator(var rules: MutableList<ScheduledAvailabilitySegmentRule>) {
    fun validate(item: ScheduledAvailabilitySegment): MutableList<String> {
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

    fun requireValid(item: ScheduledAvailabilitySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAvailabilitySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAvailabilitySegmentValidator(): ScheduledAvailabilitySegmentValidator {
    return ScheduledAvailabilitySegmentValidator(
        mutableListOf<ScheduledAvailabilitySegmentRule>(
            ScheduledAvailabilitySegmentRequiredCodeRule("scheduled availability segment needs an entry"),
            ScheduledAvailabilitySegmentWindowDaysRangeRule(64, 86400),
            ScheduledAvailabilitySegmentRequiredDriverIdRule("scheduled availability segment needs an entry"),
            ScheduledAvailabilitySegmentRequiredLabelRule("scheduled availability segment needs this field"),
        ),
    )
}
