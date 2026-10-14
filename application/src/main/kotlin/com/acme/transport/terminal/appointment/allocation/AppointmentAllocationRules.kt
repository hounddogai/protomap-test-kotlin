package com.acme.transport.terminal.appointment.allocation

/** A rule a appointment allocation must pass. Check returns its problems, or none. */
interface AppointmentAllocationRule {
    fun check(item: AppointmentAllocation): MutableList<String>
}

data class AppointmentAllocationLoadKgRangeRule(var min: Double, var max: Double) : AppointmentAllocationRule {
    override fun check(item: AppointmentAllocation): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class AppointmentAllocationArchivedGuardRule(var limit: Int) : AppointmentAllocationRule {
    override fun check(item: AppointmentAllocation): MutableList<String> {
        if (item.archived && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAllocationCodeLengthRule(var maxLength: Int) : AppointmentAllocationRule {
    override fun check(item: AppointmentAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAllocationRequiredCodeRule(var message: String) : AppointmentAllocationRule {
    override fun check(item: AppointmentAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment allocation. */
data class AppointmentAllocationValidator(var rules: MutableList<AppointmentAllocationRule>) {
    fun validate(item: AppointmentAllocation): MutableList<String> {
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

    fun requireValid(item: AppointmentAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentAllocationValidator(): AppointmentAllocationValidator {
    return AppointmentAllocationValidator(
        mutableListOf<AppointmentAllocationRule>(
            AppointmentAllocationLoadKgRangeRule(9.4, 2950.5),
            AppointmentAllocationArchivedGuardRule(32),
            AppointmentAllocationCodeLengthRule(3600),
            AppointmentAllocationRequiredCodeRule("appointment allocation needs this field"),
        ),
    )
}
