package com.acme.transport.fleetops.inspection.draft

/** A rule a pending inspection schedule must pass. Check returns its problems, or none. */
interface PendingInspectionScheduleRule {
    fun check(item: PendingInspectionSchedule): MutableList<String>
}

data class PendingInspectionScheduleTitleLengthRule(var maxLength: Int) : PendingInspectionScheduleRule {
    override fun check(item: PendingInspectionSchedule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionScheduleLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionScheduleRule {
    override fun check(item: PendingInspectionSchedule): MutableList<String> {
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

data class PendingInspectionScheduleDepotLengthRule(var maxLength: Int) : PendingInspectionScheduleRule {
    override fun check(item: PendingInspectionSchedule): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection schedule. */
data class PendingInspectionScheduleValidator(var rules: MutableList<PendingInspectionScheduleRule>) {
    fun validate(item: PendingInspectionSchedule): MutableList<String> {
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

    fun requireValid(item: PendingInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionScheduleValidator(): PendingInspectionScheduleValidator {
    return PendingInspectionScheduleValidator(
        mutableListOf<PendingInspectionScheduleRule>(
            PendingInspectionScheduleTitleLengthRule(86400),
            PendingInspectionScheduleLoadKgRangeRule(7.6, 2007.1),
            PendingInspectionScheduleDepotLengthRule(16),
        ),
    )
}
