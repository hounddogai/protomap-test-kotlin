package com.acme.transport.terminal.appointment.appointmentpreference

/** A rule a appointment audit must pass. Check returns its problems, or none. */
interface AppointmentAuditRule {
    fun check(item: AppointmentAudit): MutableList<String>
}

data class AppointmentAuditNameLengthRule(var maxLength: Int) : AppointmentAuditRule {
    override fun check(item: AppointmentAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAuditArchivedGuardRule(var limit: Int) : AppointmentAuditRule {
    override fun check(item: AppointmentAudit): MutableList<String> {
        if (item.archived && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAuditBatchSizeRangeRule(var min: Int, var max: Int) : AppointmentAuditRule {
    override fun check(item: AppointmentAudit): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class AppointmentAuditDepotLengthRule(var maxLength: Int) : AppointmentAuditRule {
    override fun check(item: AppointmentAudit): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AppointmentAuditBudgetRangeRule(var min: Double, var max: Double) : AppointmentAuditRule {
    override fun check(item: AppointmentAudit): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a appointment audit. */
data class AppointmentAuditValidator(var rules: MutableList<AppointmentAuditRule>) {
    fun validate(item: AppointmentAudit): MutableList<String> {
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

    fun requireValid(item: AppointmentAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AppointmentAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAppointmentAuditValidator(): AppointmentAuditValidator {
    return AppointmentAuditValidator(
        mutableListOf<AppointmentAuditRule>(
            AppointmentAuditNameLengthRule(3600),
            AppointmentAuditArchivedGuardRule(255),
            AppointmentAuditBatchSizeRangeRule(16, 255),
            AppointmentAuditDepotLengthRule(80),
            AppointmentAuditBudgetRangeRule(9.8, 558.7),
        ),
    )
}
