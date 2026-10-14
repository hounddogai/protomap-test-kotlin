package com.acme.transport.crewing.dutyperiod.schedule

/** A rule a manual duty period slot must pass. Check returns its problems, or none. */
interface ManualDutyPeriodSlotRule {
    fun check(item: ManualDutyPeriodSlot): MutableList<String>
}

data class ManualDutyPeriodSlotAttemptsRangeRule(var min: Int, var max: Int) : ManualDutyPeriodSlotRule {
    override fun check(item: ManualDutyPeriodSlot): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodSlotVehicleIdLengthRule(var maxLength: Int) : ManualDutyPeriodSlotRule {
    override fun check(item: ManualDutyPeriodSlot): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDutyPeriodSlotLabelLengthRule(var maxLength: Int) : ManualDutyPeriodSlotRule {
    override fun check(item: ManualDutyPeriodSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual duty period slot. */
data class ManualDutyPeriodSlotValidator(var rules: MutableList<ManualDutyPeriodSlotRule>) {
    fun validate(item: ManualDutyPeriodSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualDutyPeriodSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDutyPeriodSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDutyPeriodSlotValidator(): ManualDutyPeriodSlotValidator {
    return ManualDutyPeriodSlotValidator(
        mutableListOf<ManualDutyPeriodSlotRule>(
            ManualDutyPeriodSlotAttemptsRangeRule(16, 16),
            ManualDutyPeriodSlotVehicleIdLengthRule(64),
            ManualDutyPeriodSlotLabelLengthRule(120),
        ),
    )
}
