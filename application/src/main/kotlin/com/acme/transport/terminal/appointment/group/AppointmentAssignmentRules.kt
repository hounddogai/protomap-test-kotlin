package com.acme.transport.terminal.appointment.group

/** A rule a appointment assignment must pass. Check returns its problems, or none. */
interface AppointmentAssignmentRule {
    fun check(item: AppointmentAssignment): MutableList<String>
}

data class AppointmentAssignmentCapacityRangeRule(var min: Int, var max: Int) : AppointmentAssignmentRule {
    override fun check(item: AppointmentAssignment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAssignmentRequiredNameRule(var message: String) : AppointmentAssignmentRule {
    override fun check(item: AppointmentAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAssignmentPhoneLengthRule(var maxLength: Int) : AppointmentAssignmentRule {
    override fun check(item: AppointmentAssignment): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAssignmentCodeLengthRule(var maxLength: Int) : AppointmentAssignmentRule {
    override fun check(item: AppointmentAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAssignmentRequiredCodeRule(var message: String) : AppointmentAssignmentRule {
    override fun check(item: AppointmentAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment assignment. */
data class AppointmentAssignmentValidator(var rules: MutableList<AppointmentAssignmentRule>) {
    fun validate(item: AppointmentAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AppointmentAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentAssignmentValidator(): AppointmentAssignmentValidator {
    return AppointmentAssignmentValidator(
        mutableListOf<AppointmentAssignmentRule>(
            AppointmentAssignmentCapacityRangeRule(64, 255),
            AppointmentAssignmentRequiredNameRule("appointment assignment needs a value"),
            AppointmentAssignmentPhoneLengthRule(3600),
            AppointmentAssignmentCodeLengthRule(120),
            AppointmentAssignmentRequiredCodeRule("appointment assignment needs this field"),
        ),
    )
}
