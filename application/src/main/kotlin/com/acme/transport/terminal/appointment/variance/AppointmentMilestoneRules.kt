package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize

/** A rule a appointment milestone must pass. Check returns its problems, or none. */
interface AppointmentMilestoneRule {
    fun check(item: AppointmentMilestone): MutableList<String>
}

data class AppointmentMilestoneAllowedCodeRule(var allowed: MutableList<String>) : AppointmentMilestoneRule {
    override fun check(item: AppointmentMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMilestoneCodeLengthRule(var maxLength: Int) : AppointmentMilestoneRule {
    override fun check(item: AppointmentMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMilestoneLabelLengthRule(var maxLength: Int) : AppointmentMilestoneRule {
    override fun check(item: AppointmentMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMilestoneContactEmailLengthRule(var maxLength: Int) : AppointmentMilestoneRule {
    override fun check(item: AppointmentMilestone): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentMilestoneOdometerRangeRule(var min: Int, var max: Int) : AppointmentMilestoneRule {
    override fun check(item: AppointmentMilestone): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a appointment milestone. */
data class AppointmentMilestoneValidator(var rules: MutableList<AppointmentMilestoneRule>) {
    fun validate(item: AppointmentMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentMilestoneValidator(): AppointmentMilestoneValidator {
    return AppointmentMilestoneValidator(
        mutableListOf<AppointmentMilestoneRule>(
            AppointmentMilestoneAllowedCodeRule(mutableListOf<String>("weighted", "legacy")),
            AppointmentMilestoneCodeLengthRule(80),
            AppointmentMilestoneLabelLengthRule(16),
            AppointmentMilestoneContactEmailLengthRule(3600),
            AppointmentMilestoneOdometerRangeRule(16, 1000),
        ),
    )
}
