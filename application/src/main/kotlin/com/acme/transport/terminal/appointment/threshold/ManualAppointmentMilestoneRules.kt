package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.normalize

/** A rule a manual appointment milestone must pass. Check returns its problems, or none. */
interface ManualAppointmentMilestoneRule {
    fun check(item: ManualAppointmentMilestone): MutableList<String>
}

data class ManualAppointmentMilestoneAllowedTitleRule(
    var allowed: MutableList<String>,
) : ManualAppointmentMilestoneRule {
    override fun check(item: ManualAppointmentMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualAppointmentMilestoneFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ManualAppointmentMilestoneRule {
    override fun check(item: ManualAppointmentMilestone): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ManualAppointmentMilestoneCodePrefixRule(var prefix: String) : ManualAppointmentMilestoneRule {
    override fun check(item: ManualAppointmentMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAppointmentMilestoneTitleLengthRule(var maxLength: Int) : ManualAppointmentMilestoneRule {
    override fun check(item: ManualAppointmentMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual appointment milestone. */
data class ManualAppointmentMilestoneValidator(var rules: MutableList<ManualAppointmentMilestoneRule>) {
    fun validate(item: ManualAppointmentMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAppointmentMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAppointmentMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAppointmentMilestoneValidator(): ManualAppointmentMilestoneValidator {
    return ManualAppointmentMilestoneValidator(
        mutableListOf<ManualAppointmentMilestoneRule>(
            ManualAppointmentMilestoneAllowedTitleRule(mutableListOf<String>("strict")),
            ManualAppointmentMilestoneFuelLitersRangeRule(0.2, 2660.0),
            ManualAppointmentMilestoneCodePrefixRule("X"),
            ManualAppointmentMilestoneTitleLengthRule(80),
        ),
    )
}
