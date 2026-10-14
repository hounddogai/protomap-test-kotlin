package com.acme.transport.fleetops.defect.defectprofile

/** A rule a regional defect schedule must pass. Check returns its problems, or none. */
interface RegionalDefectScheduleRule {
    fun check(item: RegionalDefectSchedule): MutableList<String>
}

data class RegionalDefectScheduleLabelLengthRule(var maxLength: Int) : RegionalDefectScheduleRule {
    override fun check(item: RegionalDefectSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectScheduleRequiredDriverIdRule(var message: String) : RegionalDefectScheduleRule {
    override fun check(item: RegionalDefectSchedule): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectScheduleRequiredContactEmailRule(var message: String) : RegionalDefectScheduleRule {
    override fun check(item: RegionalDefectSchedule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional defect schedule. */
data class RegionalDefectScheduleValidator(var rules: MutableList<RegionalDefectScheduleRule>) {
    fun validate(item: RegionalDefectSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDefectSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDefectSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDefectScheduleValidator(): RegionalDefectScheduleValidator {
    return RegionalDefectScheduleValidator(
        mutableListOf<RegionalDefectScheduleRule>(
            RegionalDefectScheduleLabelLengthRule(255),
            RegionalDefectScheduleRequiredDriverIdRule("regional defect schedule needs an entry"),
            RegionalDefectScheduleRequiredContactEmailRule("regional defect schedule needs this field"),
        ),
    )
}
