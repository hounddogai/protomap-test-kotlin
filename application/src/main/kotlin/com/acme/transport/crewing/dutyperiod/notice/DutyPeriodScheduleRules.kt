package com.acme.transport.crewing.dutyperiod.notice

/** A rule a duty period schedule must pass. Check returns its problems, or none. */
interface DutyPeriodScheduleRule {
    fun check(item: DutyPeriodSchedule): MutableList<String>
}

data class DutyPeriodScheduleLabelLengthRule(var maxLength: Int) : DutyPeriodScheduleRule {
    override fun check(item: DutyPeriodSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodScheduleRequiredTrailerIdRule(var message: String) : DutyPeriodScheduleRule {
    override fun check(item: DutyPeriodSchedule): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodScheduleCodeLengthRule(var maxLength: Int) : DutyPeriodScheduleRule {
    override fun check(item: DutyPeriodSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodScheduleCapacityRangeRule(var min: Int, var max: Int) : DutyPeriodScheduleRule {
    override fun check(item: DutyPeriodSchedule): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodScheduleRequiredLabelRule(var message: String) : DutyPeriodScheduleRule {
    override fun check(item: DutyPeriodSchedule): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodScheduleCarrierLengthRule(var maxLength: Int) : DutyPeriodScheduleRule {
    override fun check(item: DutyPeriodSchedule): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period schedule. */
data class DutyPeriodScheduleValidator(var rules: MutableList<DutyPeriodScheduleRule>) {
    fun validate(item: DutyPeriodSchedule): MutableList<String> {
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

    fun requireValid(item: DutyPeriodSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodScheduleValidator(): DutyPeriodScheduleValidator {
    return DutyPeriodScheduleValidator(
        mutableListOf<DutyPeriodScheduleRule>(
            DutyPeriodScheduleLabelLengthRule(86400),
            DutyPeriodScheduleRequiredTrailerIdRule("duty period schedule needs a value"),
            DutyPeriodScheduleCodeLengthRule(3600),
            DutyPeriodScheduleCapacityRangeRule(120, 3600),
            DutyPeriodScheduleRequiredLabelRule("duty period schedule needs a value"),
            DutyPeriodScheduleCarrierLengthRule(32),
        ),
    )
}
