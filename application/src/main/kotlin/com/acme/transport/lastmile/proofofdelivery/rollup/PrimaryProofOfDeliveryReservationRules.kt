package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.text.normalize

/** A rule a primary proof of delivery reservation must pass. Check returns its problems, or none. */
interface PrimaryProofOfDeliveryReservationRule {
    fun check(item: PrimaryProofOfDeliveryReservation): MutableList<String>
}

data class PrimaryProofOfDeliveryReservationRequiredUnitRule(
    var message: String,
) : PrimaryProofOfDeliveryReservationRule {
    override fun check(item: PrimaryProofOfDeliveryReservation): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryReservationCountryLengthRule(
    var maxLength: Int,
) : PrimaryProofOfDeliveryReservationRule {
    override fun check(item: PrimaryProofOfDeliveryReservation): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryReservationActiveGuardRule(
    var limit: Double,
) : PrimaryProofOfDeliveryReservationRule {
    override fun check(item: PrimaryProofOfDeliveryReservation): MutableList<String> {
        if (item.active && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryReservationAllowedCountryRule(
    var allowed: MutableList<String>,
) : PrimaryProofOfDeliveryReservationRule {
    override fun check(item: PrimaryProofOfDeliveryReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryProofOfDeliveryReservationTitleLengthRule(
    var maxLength: Int,
) : PrimaryProofOfDeliveryReservationRule {
    override fun check(item: PrimaryProofOfDeliveryReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary proof of delivery reservation. */
data class PrimaryProofOfDeliveryReservationValidator(
    var rules: MutableList<PrimaryProofOfDeliveryReservationRule>,
) {
    fun validate(item: PrimaryProofOfDeliveryReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryProofOfDeliveryReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryProofOfDeliveryReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryProofOfDeliveryReservationValidator(): PrimaryProofOfDeliveryReservationValidator {
    return PrimaryProofOfDeliveryReservationValidator(
        mutableListOf<PrimaryProofOfDeliveryReservationRule>(
            PrimaryProofOfDeliveryReservationRequiredUnitRule(
                "primary proof of delivery reservation needs a value",
            ),
            PrimaryProofOfDeliveryReservationCountryLengthRule(1000),
            PrimaryProofOfDeliveryReservationActiveGuardRule(5.5),
            PrimaryProofOfDeliveryReservationAllowedCountryRule(mutableListOf<String>("default", "scheduled")),
            PrimaryProofOfDeliveryReservationTitleLengthRule(64),
        ),
    )
}
