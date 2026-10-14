package com.acme.transport.terminal.appointment.metric

/** A rule a pending appointment audit must pass. Check returns its problems, or none. */
interface PendingAppointmentAuditRule {
    fun check(item: PendingAppointmentAudit): MutableList<String>
}

data class PendingAppointmentAuditCapacityRangeRule(var min: Int, var max: Int) : PendingAppointmentAuditRule {
    override fun check(item: PendingAppointmentAudit): MutableList<String> {
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

data class PendingAppointmentAuditCodeLengthRule(var maxLength: Int) : PendingAppointmentAuditRule {
    override fun check(item: PendingAppointmentAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentAuditRequiredNameRule(var message: String) : PendingAppointmentAuditRule {
    override fun check(item: PendingAppointmentAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending appointment audit. */
data class PendingAppointmentAuditValidator(var rules: MutableList<PendingAppointmentAuditRule>) {
    fun validate(item: PendingAppointmentAudit): MutableList<String> {
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

    fun requireValid(item: PendingAppointmentAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAppointmentAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAppointmentAuditValidator(): PendingAppointmentAuditValidator {
    return PendingAppointmentAuditValidator(
        mutableListOf<PendingAppointmentAuditRule>(
            PendingAppointmentAuditCapacityRangeRule(16, 64),
            PendingAppointmentAuditCodeLengthRule(86400),
            PendingAppointmentAuditRequiredNameRule("pending appointment audit needs this field"),
        ),
    )
}
