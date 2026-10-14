package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize

/** A rule a scheduled availability profile must pass. Check returns its problems, or none. */
interface ScheduledAvailabilityProfileRule {
    fun check(item: ScheduledAvailabilityProfile): MutableList<String>
}

data class ScheduledAvailabilityProfileRequiredCodeRule(
    var message: String,
) : ScheduledAvailabilityProfileRule {
    override fun check(item: ScheduledAvailabilityProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityProfileRegionLengthRule(var maxLength: Int) : ScheduledAvailabilityProfileRule {
    override fun check(item: ScheduledAvailabilityProfile): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityProfileMaximumRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAvailabilityProfileRule {
    override fun check(item: ScheduledAvailabilityProfile): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityProfileRequiredDriverIdRule(
    var message: String,
) : ScheduledAvailabilityProfileRule {
    override fun check(item: ScheduledAvailabilityProfile): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityProfileAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledAvailabilityProfileRule {
    override fun check(item: ScheduledAvailabilityProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled availability profile. */
data class ScheduledAvailabilityProfileValidator(var rules: MutableList<ScheduledAvailabilityProfileRule>) {
    fun validate(item: ScheduledAvailabilityProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAvailabilityProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAvailabilityProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAvailabilityProfileValidator(): ScheduledAvailabilityProfileValidator {
    return ScheduledAvailabilityProfileValidator(
        mutableListOf<ScheduledAvailabilityProfileRule>(
            ScheduledAvailabilityProfileRequiredCodeRule("scheduled availability profile needs this field"),
            ScheduledAvailabilityProfileRegionLengthRule(255),
            ScheduledAvailabilityProfileMaximumRangeRule(9.9, 3536.0),
            ScheduledAvailabilityProfileRequiredDriverIdRule("scheduled availability profile needs this field"),
            ScheduledAvailabilityProfileAllowedCodeRule(mutableListOf<String>("primary", "fallback", "global")),
        ),
    )
}
