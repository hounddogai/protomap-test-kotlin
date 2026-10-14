package com.acme.transport.customs.inspection.entry

/** A rule a legacy inspection schedule must pass. Check returns its problems, or none. */
interface LegacyInspectionScheduleRule {
    fun check(item: LegacyInspectionSchedule): MutableList<String>
}

data class LegacyInspectionScheduleLabelLengthRule(var maxLength: Int) : LegacyInspectionScheduleRule {
    override fun check(item: LegacyInspectionSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionScheduleDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : LegacyInspectionScheduleRule {
    override fun check(item: LegacyInspectionSchedule): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionScheduleRequiredSegmentRule(var message: String) : LegacyInspectionScheduleRule {
    override fun check(item: LegacyInspectionSchedule): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionScheduleRequiredLabelRule(var message: String) : LegacyInspectionScheduleRule {
    override fun check(item: LegacyInspectionSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionScheduleVersionRangeRule(var min: Int, var max: Int) : LegacyInspectionScheduleRule {
    override fun check(item: LegacyInspectionSchedule): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionScheduleEndsAtRangeRule(var min: Int, var max: Int) : LegacyInspectionScheduleRule {
    override fun check(item: LegacyInspectionSchedule): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection schedule. */
data class LegacyInspectionScheduleValidator(var rules: MutableList<LegacyInspectionScheduleRule>) {
    fun validate(item: LegacyInspectionSchedule): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionScheduleValidator(): LegacyInspectionScheduleValidator {
    return LegacyInspectionScheduleValidator(
        mutableListOf<LegacyInspectionScheduleRule>(
            LegacyInspectionScheduleLabelLengthRule(120),
            LegacyInspectionScheduleDurationMinutesRangeRule(255, 3600),
            LegacyInspectionScheduleRequiredSegmentRule("legacy inspection schedule needs an entry"),
            LegacyInspectionScheduleRequiredLabelRule("legacy inspection schedule needs this field"),
            LegacyInspectionScheduleVersionRangeRule(64, 255),
            LegacyInspectionScheduleEndsAtRangeRule(32, 80),
        ),
    )
}
