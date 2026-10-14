package com.acme.transport.freightpay.dispute.schedule

/** A rule a dispute schedule must pass. Check returns its problems, or none. */
interface DisputeScheduleRule {
    fun check(item: DisputeSchedule): MutableList<String>
}

data class DisputeScheduleMaximumRangeRule(var min: Double, var max: Double) : DisputeScheduleRule {
    override fun check(item: DisputeSchedule): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeScheduleLabelLengthRule(var maxLength: Int) : DisputeScheduleRule {
    override fun check(item: DisputeSchedule): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeScheduleVehicleIdLengthRule(var maxLength: Int) : DisputeScheduleRule {
    override fun check(item: DisputeSchedule): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeScheduleUpdatedAtRangeRule(var min: Int, var max: Int) : DisputeScheduleRule {
    override fun check(item: DisputeSchedule): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeScheduleCodePrefixRule(var prefix: String) : DisputeScheduleRule {
    override fun check(item: DisputeSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute schedule. */
data class DisputeScheduleValidator(var rules: MutableList<DisputeScheduleRule>) {
    fun validate(item: DisputeSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeScheduleValidator(): DisputeScheduleValidator {
    return DisputeScheduleValidator(
        mutableListOf<DisputeScheduleRule>(
            DisputeScheduleMaximumRangeRule(5.1, 3527.8),
            DisputeScheduleLabelLengthRule(1000),
            DisputeScheduleVehicleIdLengthRule(1000),
            DisputeScheduleUpdatedAtRangeRule(1000, 3600),
            DisputeScheduleCodePrefixRule("OPS"),
        ),
    )
}
