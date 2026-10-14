package com.acme.transport.crewing.availability.availabilityexception

import com.acme.transport.kit.text.normalize

/** A rule a availability schedule must pass. Check returns its problems, or none. */
interface AvailabilityScheduleRule {
    fun check(item: AvailabilitySchedule): MutableList<String>
}

data class AvailabilityScheduleTitleLengthRule(var maxLength: Int) : AvailabilityScheduleRule {
    override fun check(item: AvailabilitySchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityScheduleAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : AvailabilityScheduleRule {
    override fun check(item: AvailabilitySchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityScheduleApprovedGuardRule(var limit: Int) : AvailabilityScheduleRule {
    override fun check(item: AvailabilitySchedule): MutableList<String> {
        if (item.approved && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityScheduleRequiredNameRule(var message: String) : AvailabilityScheduleRule {
    override fun check(item: AvailabilitySchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability schedule. */
data class AvailabilityScheduleValidator(var rules: MutableList<AvailabilityScheduleRule>) {
    fun validate(item: AvailabilitySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilitySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilitySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityScheduleValidator(): AvailabilityScheduleValidator {
    return AvailabilityScheduleValidator(
        mutableListOf<AvailabilityScheduleRule>(
            AvailabilityScheduleTitleLengthRule(3600),
            AvailabilityScheduleAllowedShiftCodeRule(mutableListOf<String>("strict", "batched", "primary")),
            AvailabilityScheduleApprovedGuardRule(3600),
            AvailabilityScheduleRequiredNameRule("availability schedule needs a value"),
        ),
    )
}
