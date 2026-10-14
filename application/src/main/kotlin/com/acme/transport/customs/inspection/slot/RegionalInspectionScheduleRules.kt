package com.acme.transport.customs.inspection.slot

/** A rule a regional inspection schedule must pass. Check returns its problems, or none. */
interface RegionalInspectionScheduleRule {
    fun check(item: RegionalInspectionSchedule): MutableList<String>
}

data class RegionalInspectionScheduleRequiredReviewerRule(
    var message: String,
) : RegionalInspectionScheduleRule {
    override fun check(item: RegionalInspectionSchedule): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionScheduleCodeLengthRule(var maxLength: Int) : RegionalInspectionScheduleRule {
    override fun check(item: RegionalInspectionSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionScheduleRequiredCodeRule(var message: String) : RegionalInspectionScheduleRule {
    override fun check(item: RegionalInspectionSchedule): MutableList<String> {
        if (item.code.trim() == "") {
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
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
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
            RegionalInspectionScheduleRequiredReviewerRule("regional inspection schedule needs this field"),
            RegionalInspectionScheduleCodeLengthRule(32),
            RegionalInspectionScheduleRequiredCodeRule("regional inspection schedule needs a value"),
        ),
    )
}
