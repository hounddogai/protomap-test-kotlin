package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.text.normalize

/** A rule a seasonal appointment audit must pass. Check returns its problems, or none. */
interface SeasonalAppointmentAuditRule {
    fun check(item: SeasonalAppointmentAudit): MutableList<String>
}

data class SeasonalAppointmentAuditRequiredSegmentRule(var message: String) : SeasonalAppointmentAuditRule {
    override fun check(item: SeasonalAppointmentAudit): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAppointmentAuditVehicleIdLengthRule(var maxLength: Int) : SeasonalAppointmentAuditRule {
    override fun check(item: SeasonalAppointmentAudit): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAppointmentAuditAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalAppointmentAuditRule {
    override fun check(item: SeasonalAppointmentAudit): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAppointmentAuditRequiredCodeRule(var message: String) : SeasonalAppointmentAuditRule {
    override fun check(item: SeasonalAppointmentAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal appointment audit. */
data class SeasonalAppointmentAuditValidator(var rules: MutableList<SeasonalAppointmentAuditRule>) {
    fun validate(item: SeasonalAppointmentAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAppointmentAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAppointmentAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAppointmentAuditValidator(): SeasonalAppointmentAuditValidator {
    return SeasonalAppointmentAuditValidator(
        mutableListOf<SeasonalAppointmentAuditRule>(
            SeasonalAppointmentAuditRequiredSegmentRule("seasonal appointment audit needs this field"),
            SeasonalAppointmentAuditVehicleIdLengthRule(64),
            SeasonalAppointmentAuditAllowedLabelRule(mutableListOf<String>("legacy", "tiered", "lenient")),
            SeasonalAppointmentAuditRequiredCodeRule("seasonal appointment audit needs an entry"),
        ),
    )
}
