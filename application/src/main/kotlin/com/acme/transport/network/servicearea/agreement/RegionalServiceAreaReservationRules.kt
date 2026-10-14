package com.acme.transport.network.servicearea.agreement

/** A rule a regional service area reservation must pass. Check returns its problems, or none. */
interface RegionalServiceAreaReservationRule {
    fun check(item: RegionalServiceAreaReservation): MutableList<String>
}

data class RegionalServiceAreaReservationExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : RegionalServiceAreaReservationRule {
    override fun check(item: RegionalServiceAreaReservation): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class RegionalServiceAreaReservationSequenceRangeRule(
    var min: Int,
    var max: Int,
) : RegionalServiceAreaReservationRule {
    override fun check(item: RegionalServiceAreaReservation): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class RegionalServiceAreaReservationDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : RegionalServiceAreaReservationRule {
    override fun check(item: RegionalServiceAreaReservation): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional service area reservation. */
data class RegionalServiceAreaReservationValidator(var rules: MutableList<RegionalServiceAreaReservationRule>) {
    fun validate(item: RegionalServiceAreaReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalServiceAreaReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalServiceAreaReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalServiceAreaReservationValidator(): RegionalServiceAreaReservationValidator {
    return RegionalServiceAreaReservationValidator(
        mutableListOf<RegionalServiceAreaReservationRule>(
            RegionalServiceAreaReservationExpiresAtRangeRule(80, 1000),
            RegionalServiceAreaReservationSequenceRangeRule(255, 3600),
            RegionalServiceAreaReservationDistanceKmRangeRule(7.7, 3435.2),
        ),
    )
}
