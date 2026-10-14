package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.normalize

/** A rule a adaptive inspection schedule must pass. Check returns its problems, or none. */
interface AdaptiveInspectionScheduleRule {
    fun check(item: AdaptiveInspectionSchedule): MutableList<String>
}

data class AdaptiveInspectionScheduleRequiredDescriptionRule(
    var message: String,
) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleCodeLengthRule(var maxLength: Int) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleApprovedGuardRule(var limit: Int) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is too long")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleDescriptionLengthRule(
    var maxLength: Int,
) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionScheduleTitleLengthRule(var maxLength: Int) : AdaptiveInspectionScheduleRule {
    override fun check(item: AdaptiveInspectionSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection schedule. */
data class AdaptiveInspectionScheduleValidator(var rules: MutableList<AdaptiveInspectionScheduleRule>) {
    fun validate(item: AdaptiveInspectionSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionScheduleValidator(): AdaptiveInspectionScheduleValidator {
    return AdaptiveInspectionScheduleValidator(
        mutableListOf<AdaptiveInspectionScheduleRule>(
            AdaptiveInspectionScheduleRequiredDescriptionRule("adaptive inspection schedule needs an entry"),
            AdaptiveInspectionScheduleCodeLengthRule(64),
            AdaptiveInspectionScheduleApprovedGuardRule(255),
            AdaptiveInspectionScheduleAllowedVehicleIdRule(mutableListOf<String>("global")),
            AdaptiveInspectionScheduleDescriptionLengthRule(3600),
            AdaptiveInspectionScheduleTitleLengthRule(3600),
        ),
    )
}
