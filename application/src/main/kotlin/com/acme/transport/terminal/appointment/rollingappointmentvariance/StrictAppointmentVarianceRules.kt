package com.acme.transport.terminal.appointment.rollingappointmentvariance

/** A rule a strict appointment variance must pass. Check returns its problems, or none. */
interface StrictAppointmentVarianceRule {
    fun check(item: StrictAppointmentVariance): MutableList<String>
}

data class StrictAppointmentVarianceRequiredNameRule(var message: String) : StrictAppointmentVarianceRule {
    override fun check(item: StrictAppointmentVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentVarianceRequiredUnitRule(var message: String) : StrictAppointmentVarianceRule {
    override fun check(item: StrictAppointmentVariance): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictAppointmentVarianceNameLengthRule(var maxLength: Int) : StrictAppointmentVarianceRule {
    override fun check(item: StrictAppointmentVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict appointment variance. */
data class StrictAppointmentVarianceValidator(var rules: MutableList<StrictAppointmentVarianceRule>) {
    fun validate(item: StrictAppointmentVariance): MutableList<String> {
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

    fun requireValid(item: StrictAppointmentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictAppointmentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictAppointmentVarianceValidator(): StrictAppointmentVarianceValidator {
    return StrictAppointmentVarianceValidator(
        mutableListOf<StrictAppointmentVarianceRule>(
            StrictAppointmentVarianceRequiredNameRule("strict appointment variance needs an entry"),
            StrictAppointmentVarianceRequiredUnitRule("strict appointment variance needs an entry"),
            StrictAppointmentVarianceNameLengthRule(3600),
        ),
    )
}
