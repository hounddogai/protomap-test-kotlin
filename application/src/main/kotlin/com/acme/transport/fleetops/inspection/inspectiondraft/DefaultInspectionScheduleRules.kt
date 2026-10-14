package com.acme.transport.fleetops.inspection.inspectiondraft

/** A rule a default inspection schedule must pass. Check returns its problems, or none. */
interface DefaultInspectionScheduleRule {
    fun check(item: DefaultInspectionSchedule): MutableList<String>
}

data class DefaultInspectionScheduleCodeLengthRule(var maxLength: Int) : DefaultInspectionScheduleRule {
    override fun check(item: DefaultInspectionSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionScheduleRequiredLabelRule(var message: String) : DefaultInspectionScheduleRule {
    override fun check(item: DefaultInspectionSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionScheduleTimezoneLengthRule(var maxLength: Int) : DefaultInspectionScheduleRule {
    override fun check(item: DefaultInspectionSchedule): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionScheduleRequiredCodeRule(var message: String) : DefaultInspectionScheduleRule {
    override fun check(item: DefaultInspectionSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultInspectionScheduleGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : DefaultInspectionScheduleRule {
    override fun check(item: DefaultInspectionSchedule): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default inspection schedule. */
data class DefaultInspectionScheduleValidator(var rules: MutableList<DefaultInspectionScheduleRule>) {
    fun validate(item: DefaultInspectionSchedule): MutableList<String> {
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

    fun requireValid(item: DefaultInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultInspectionScheduleValidator(): DefaultInspectionScheduleValidator {
    return DefaultInspectionScheduleValidator(
        mutableListOf<DefaultInspectionScheduleRule>(
            DefaultInspectionScheduleCodeLengthRule(80),
            DefaultInspectionScheduleRequiredLabelRule("default inspection schedule needs a value"),
            DefaultInspectionScheduleTimezoneLengthRule(120),
            DefaultInspectionScheduleRequiredCodeRule("default inspection schedule needs a value"),
            DefaultInspectionScheduleGraceDaysRangeRule(32, 3600),
        ),
    )
}
