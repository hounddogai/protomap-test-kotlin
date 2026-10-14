package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.normalize

/** A rule a lenient appointment audit must pass. Check returns its problems, or none. */
interface LenientAppointmentAuditRule {
    fun check(item: LenientAppointmentAudit): MutableList<String>
}

data class LenientAppointmentAuditRequiredCodeRule(var message: String) : LenientAppointmentAuditRule {
    override fun check(item: LenientAppointmentAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentAuditLabelLengthRule(var maxLength: Int) : LenientAppointmentAuditRule {
    override fun check(item: LenientAppointmentAudit): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentAuditThresholdRangeRule(
    var min: Double,
    var max: Double,
) : LenientAppointmentAuditRule {
    override fun check(item: LenientAppointmentAudit): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class LenientAppointmentAuditCodeLengthRule(var maxLength: Int) : LenientAppointmentAuditRule {
    override fun check(item: LenientAppointmentAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentAuditAllowedNotesRule(
    var allowed: MutableList<String>,
) : LenientAppointmentAuditRule {
    override fun check(item: LenientAppointmentAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class LenientAppointmentAuditRequiredTitleRule(var message: String) : LenientAppointmentAuditRule {
    override fun check(item: LenientAppointmentAudit): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient appointment audit. */
data class LenientAppointmentAuditValidator(var rules: MutableList<LenientAppointmentAuditRule>) {
    fun validate(item: LenientAppointmentAudit): MutableList<String> {
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

    fun requireValid(item: LenientAppointmentAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAppointmentAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAppointmentAuditValidator(): LenientAppointmentAuditValidator {
    return LenientAppointmentAuditValidator(
        mutableListOf<LenientAppointmentAuditRule>(
            LenientAppointmentAuditRequiredCodeRule("lenient appointment audit needs a value"),
            LenientAppointmentAuditLabelLengthRule(3600),
            LenientAppointmentAuditThresholdRangeRule(9.2, 4571.9),
            LenientAppointmentAuditCodeLengthRule(86400),
            LenientAppointmentAuditAllowedNotesRule(mutableListOf<String>("primary")),
            LenientAppointmentAuditRequiredTitleRule("lenient appointment audit needs a value"),
        ),
    )
}
