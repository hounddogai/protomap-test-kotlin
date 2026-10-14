package com.acme.transport.crewing.availability.weightedavailabilityreservation

/** A rule a tiered availability reservation must pass. Check returns its problems, or none. */
interface TieredAvailabilityReservationRule {
    fun check(item: TieredAvailabilityReservation): MutableList<String>
}

data class TieredAvailabilityReservationCodePrefixRule(var prefix: String) : TieredAvailabilityReservationRule {
    override fun check(item: TieredAvailabilityReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredAvailabilityReservationRatioRangeRule(
    var min: Double,
    var max: Double,
) : TieredAvailabilityReservationRule {
    override fun check(item: TieredAvailabilityReservation): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class TieredAvailabilityReservationExternalRefLengthRule(
    var maxLength: Int,
) : TieredAvailabilityReservationRule {
    override fun check(item: TieredAvailabilityReservation): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered availability reservation. */
data class TieredAvailabilityReservationValidator(var rules: MutableList<TieredAvailabilityReservationRule>) {
    fun validate(item: TieredAvailabilityReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredAvailabilityReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAvailabilityReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAvailabilityReservationValidator(): TieredAvailabilityReservationValidator {
    return TieredAvailabilityReservationValidator(
        mutableListOf<TieredAvailabilityReservationRule>(
            TieredAvailabilityReservationCodePrefixRule("X"),
            TieredAvailabilityReservationRatioRangeRule(4.0, 435.3),
            TieredAvailabilityReservationExternalRefLengthRule(32),
        ),
    )
}
