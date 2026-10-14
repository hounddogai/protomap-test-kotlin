package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize

/** A rule a pending appointment cycle must pass. Check returns its problems, or none. */
interface PendingAppointmentCycleRule {
    fun check(item: PendingAppointmentCycle): MutableList<String>
}

data class PendingAppointmentCycleAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingAppointmentCycleRule {
    override fun check(item: PendingAppointmentCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentCycleOdometerRangeRule(var min: Int, var max: Int) : PendingAppointmentCycleRule {
    override fun check(item: PendingAppointmentCycle): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentCycleRequiredCodeRule(var message: String) : PendingAppointmentCycleRule {
    override fun check(item: PendingAppointmentCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentCycleRevisionRangeRule(var min: Int, var max: Int) : PendingAppointmentCycleRule {
    override fun check(item: PendingAppointmentCycle): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentCycleVehicleIdLengthRule(var maxLength: Int) : PendingAppointmentCycleRule {
    override fun check(item: PendingAppointmentCycle): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending appointment cycle. */
data class PendingAppointmentCycleValidator(var rules: MutableList<PendingAppointmentCycleRule>) {
    fun validate(item: PendingAppointmentCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAppointmentCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAppointmentCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAppointmentCycleValidator(): PendingAppointmentCycleValidator {
    return PendingAppointmentCycleValidator(
        mutableListOf<PendingAppointmentCycleRule>(
            PendingAppointmentCycleAllowedCodeRule(mutableListOf<String>("primary", "fallback")),
            PendingAppointmentCycleOdometerRangeRule(80, 3600),
            PendingAppointmentCycleRequiredCodeRule("pending appointment cycle needs this field"),
            PendingAppointmentCycleRevisionRangeRule(64, 255),
            PendingAppointmentCycleVehicleIdLengthRule(32),
        ),
    )
}
