package com.acme.transport.fleetops.inspection.slot

/** A rule a regional inspection schedule must pass. Check returns its problems, or none. */
interface RegionalInspectionScheduleRule {
    fun check(item: RegionalInspectionSchedule): MutableList<String>
}

data class RegionalInspectionScheduleCodePrefixRule(var prefix: String) : RegionalInspectionScheduleRule {
    override fun check(item: RegionalInspectionSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionSchedulePriorityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalInspectionScheduleRule {
    override fun check(item: RegionalInspectionSchedule): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RegionalInspectionScheduleRequiredLabelRule(var message: String) : RegionalInspectionScheduleRule {
    override fun check(item: RegionalInspectionSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional inspection schedule. */
data class RegionalInspectionScheduleValidator(var rules: MutableList<RegionalInspectionScheduleRule>) {
    fun validate(item: RegionalInspectionSchedule): MutableList<String> {
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

    fun requireValid(item: RegionalInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalInspectionScheduleValidator(): RegionalInspectionScheduleValidator {
    return RegionalInspectionScheduleValidator(
        mutableListOf<RegionalInspectionScheduleRule>(
            RegionalInspectionScheduleCodePrefixRule("X"),
            RegionalInspectionSchedulePriorityRangeRule(16, 120),
            RegionalInspectionScheduleRequiredLabelRule("regional inspection schedule needs an entry"),
        ),
    )
}
