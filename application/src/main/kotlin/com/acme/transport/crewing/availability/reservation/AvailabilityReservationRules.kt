package com.acme.transport.crewing.availability.reservation

/** A rule a availability reservation must pass. Check returns its problems, or none. */
interface AvailabilityReservationRule {
    fun check(item: AvailabilityReservation): MutableList<String>
}

data class AvailabilityReservationRequiredCodeRule(var message: String) : AvailabilityReservationRule {
    override fun check(item: AvailabilityReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReservationCeilingRangeRule(
    var min: Double,
    var max: Double,
) : AvailabilityReservationRule {
    override fun check(item: AvailabilityReservation): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReservationCodeLengthRule(var maxLength: Int) : AvailabilityReservationRule {
    override fun check(item: AvailabilityReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityReservationCodePrefixRule(var prefix: String) : AvailabilityReservationRule {
    override fun check(item: AvailabilityReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability reservation. */
data class AvailabilityReservationValidator(var rules: MutableList<AvailabilityReservationRule>) {
    fun validate(item: AvailabilityReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityReservationValidator(): AvailabilityReservationValidator {
    return AvailabilityReservationValidator(
        mutableListOf<AvailabilityReservationRule>(
            AvailabilityReservationRequiredCodeRule("availability reservation needs a value"),
            AvailabilityReservationCeilingRangeRule(4.5, 2082.3),
            AvailabilityReservationCodeLengthRule(80),
            AvailabilityReservationCodePrefixRule("REF"),
        ),
    )
}
