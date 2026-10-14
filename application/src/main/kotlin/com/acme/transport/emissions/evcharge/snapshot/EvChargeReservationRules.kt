package com.acme.transport.emissions.evcharge.snapshot

/** A rule a ev charge reservation must pass. Check returns its problems, or none. */
interface EvChargeReservationRule {
    fun check(item: EvChargeReservation): MutableList<String>
}

data class EvChargeReservationCountryLengthRule(var maxLength: Int) : EvChargeReservationRule {
    override fun check(item: EvChargeReservation): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeReservationLabelLengthRule(var maxLength: Int) : EvChargeReservationRule {
    override fun check(item: EvChargeReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeReservationDurationMinutesRangeRule(var min: Int, var max: Int) : EvChargeReservationRule {
    override fun check(item: EvChargeReservation): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a ev charge reservation. */
data class EvChargeReservationValidator(var rules: MutableList<EvChargeReservationRule>) {
    fun validate(item: EvChargeReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeReservationValidator(): EvChargeReservationValidator {
    return EvChargeReservationValidator(
        mutableListOf<EvChargeReservationRule>(
            EvChargeReservationCountryLengthRule(64),
            EvChargeReservationLabelLengthRule(32),
            EvChargeReservationDurationMinutesRangeRule(1000, 3600),
        ),
    )
}
