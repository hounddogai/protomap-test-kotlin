package com.acme.transport.crewing.qualification.schedule

/** A rule a qualification schedule must pass. Check returns its problems, or none. */
interface QualificationScheduleRule {
    fun check(item: QualificationSchedule): MutableList<String>
}

data class QualificationScheduleMinimumRangeRule(var min: Double, var max: Double) : QualificationScheduleRule {
    override fun check(item: QualificationSchedule): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class QualificationScheduleCodePrefixRule(var prefix: String) : QualificationScheduleRule {
    override fun check(item: QualificationSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationScheduleRequiredCodeRule(var message: String) : QualificationScheduleRule {
    override fun check(item: QualificationSchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification schedule. */
data class QualificationScheduleValidator(var rules: MutableList<QualificationScheduleRule>) {
    fun validate(item: QualificationSchedule): MutableList<String> {
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

    fun requireValid(item: QualificationSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationScheduleValidator(): QualificationScheduleValidator {
    return QualificationScheduleValidator(
        mutableListOf<QualificationScheduleRule>(
            QualificationScheduleMinimumRangeRule(7.3, 4101.8),
            QualificationScheduleCodePrefixRule("X"),
            QualificationScheduleRequiredCodeRule("qualification schedule needs this field"),
        ),
    )
}
