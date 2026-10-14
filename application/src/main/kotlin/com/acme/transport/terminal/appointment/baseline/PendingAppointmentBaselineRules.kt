package com.acme.transport.terminal.appointment.baseline

/** A rule a pending appointment baseline must pass. Check returns its problems, or none. */
interface PendingAppointmentBaselineRule {
    fun check(item: PendingAppointmentBaseline): MutableList<String>
}

data class PendingAppointmentBaselineCodeLengthRule(var maxLength: Int) : PendingAppointmentBaselineRule {
    override fun check(item: PendingAppointmentBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentBaselinePhoneLengthRule(var maxLength: Int) : PendingAppointmentBaselineRule {
    override fun check(item: PendingAppointmentBaseline): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentBaselineRequiredPhoneRule(var message: String) : PendingAppointmentBaselineRule {
    override fun check(item: PendingAppointmentBaseline): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentBaselineOdometerRangeRule(
    var min: Int,
    var max: Int,
) : PendingAppointmentBaselineRule {
    override fun check(item: PendingAppointmentBaseline): MutableList<String> {
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

data class PendingAppointmentBaselineReviewerLengthRule(var maxLength: Int) : PendingAppointmentBaselineRule {
    override fun check(item: PendingAppointmentBaseline): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAppointmentBaselineFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PendingAppointmentBaselineRule {
    override fun check(item: PendingAppointmentBaseline): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending appointment baseline. */
data class PendingAppointmentBaselineValidator(var rules: MutableList<PendingAppointmentBaselineRule>) {
    fun validate(item: PendingAppointmentBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAppointmentBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAppointmentBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAppointmentBaselineValidator(): PendingAppointmentBaselineValidator {
    return PendingAppointmentBaselineValidator(
        mutableListOf<PendingAppointmentBaselineRule>(
            PendingAppointmentBaselineCodeLengthRule(3600),
            PendingAppointmentBaselinePhoneLengthRule(3600),
            PendingAppointmentBaselineRequiredPhoneRule("pending appointment baseline needs this field"),
            PendingAppointmentBaselineOdometerRangeRule(32, 120),
            PendingAppointmentBaselineReviewerLengthRule(86400),
            PendingAppointmentBaselineFloorValueRangeRule(2.7, 4551.4),
        ),
    )
}
