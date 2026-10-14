package com.acme.transport.network.zone.threshold

/** A rule a strict zone schedule must pass. Check returns its problems, or none. */
interface StrictZoneScheduleRule {
    fun check(item: StrictZoneSchedule): MutableList<String>
}

data class StrictZoneScheduleDiscountRangeRule(var min: Double, var max: Double) : StrictZoneScheduleRule {
    override fun check(item: StrictZoneSchedule): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class StrictZoneScheduleCodeLengthRule(var maxLength: Int) : StrictZoneScheduleRule {
    override fun check(item: StrictZoneSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneScheduleRequiredLabelRule(var message: String) : StrictZoneScheduleRule {
    override fun check(item: StrictZoneSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneScheduleRequiredNotesRule(var message: String) : StrictZoneScheduleRule {
    override fun check(item: StrictZoneSchedule): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict zone schedule. */
data class StrictZoneScheduleValidator(var rules: MutableList<StrictZoneScheduleRule>) {
    fun validate(item: StrictZoneSchedule): MutableList<String> {
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

    fun requireValid(item: StrictZoneSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictZoneSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictZoneScheduleValidator(): StrictZoneScheduleValidator {
    return StrictZoneScheduleValidator(
        mutableListOf<StrictZoneScheduleRule>(
            StrictZoneScheduleDiscountRangeRule(9.5, 4132.9),
            StrictZoneScheduleCodeLengthRule(16),
            StrictZoneScheduleRequiredLabelRule("strict zone schedule needs this field"),
            StrictZoneScheduleRequiredNotesRule("strict zone schedule needs this field"),
        ),
    )
}
