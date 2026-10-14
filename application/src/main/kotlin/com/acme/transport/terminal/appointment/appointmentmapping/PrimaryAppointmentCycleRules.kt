package com.acme.transport.terminal.appointment.appointmentmapping

/** A rule a primary appointment cycle must pass. Check returns its problems, or none. */
interface PrimaryAppointmentCycleRule {
    fun check(item: PrimaryAppointmentCycle): MutableList<String>
}

data class PrimaryAppointmentCycleCodePrefixRule(var prefix: String) : PrimaryAppointmentCycleRule {
    override fun check(item: PrimaryAppointmentCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentCycleRevisionRangeRule(var min: Int, var max: Int) : PrimaryAppointmentCycleRule {
    override fun check(item: PrimaryAppointmentCycle): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class PrimaryAppointmentCycleLabelLengthRule(var maxLength: Int) : PrimaryAppointmentCycleRule {
    override fun check(item: PrimaryAppointmentCycle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentCycleVehicleIdLengthRule(var maxLength: Int) : PrimaryAppointmentCycleRule {
    override fun check(item: PrimaryAppointmentCycle): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAppointmentCycleUpdatedAtRangeRule(var min: Int, var max: Int) : PrimaryAppointmentCycleRule {
    override fun check(item: PrimaryAppointmentCycle): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary appointment cycle. */
data class PrimaryAppointmentCycleValidator(var rules: MutableList<PrimaryAppointmentCycleRule>) {
    fun validate(item: PrimaryAppointmentCycle): MutableList<String> {
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

    fun requireValid(item: PrimaryAppointmentCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAppointmentCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAppointmentCycleValidator(): PrimaryAppointmentCycleValidator {
    return PrimaryAppointmentCycleValidator(
        mutableListOf<PrimaryAppointmentCycleRule>(
            PrimaryAppointmentCycleCodePrefixRule("REF"),
            PrimaryAppointmentCycleRevisionRangeRule(64, 64),
            PrimaryAppointmentCycleLabelLengthRule(255),
            PrimaryAppointmentCycleVehicleIdLengthRule(255),
            PrimaryAppointmentCycleUpdatedAtRangeRule(80, 3600),
        ),
    )
}
