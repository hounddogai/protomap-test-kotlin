package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize

/** A rule a adaptive rate card reservation must pass. Check returns its problems, or none. */
interface AdaptiveRateCardReservationRule {
    fun check(item: AdaptiveRateCardReservation): MutableList<String>
}

data class AdaptiveRateCardReservationLaneLengthRule(var maxLength: Int) : AdaptiveRateCardReservationRule {
    override fun check(item: AdaptiveRateCardReservation): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardReservationNameLengthRule(var maxLength: Int) : AdaptiveRateCardReservationRule {
    override fun check(item: AdaptiveRateCardReservation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardReservationCodePrefixRule(var prefix: String) : AdaptiveRateCardReservationRule {
    override fun check(item: AdaptiveRateCardReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRateCardReservationAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : AdaptiveRateCardReservationRule {
    override fun check(item: AdaptiveRateCardReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive rate card reservation. */
data class AdaptiveRateCardReservationValidator(var rules: MutableList<AdaptiveRateCardReservationRule>) {
    fun validate(item: AdaptiveRateCardReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveRateCardReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRateCardReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRateCardReservationValidator(): AdaptiveRateCardReservationValidator {
    return AdaptiveRateCardReservationValidator(
        mutableListOf<AdaptiveRateCardReservationRule>(
            AdaptiveRateCardReservationLaneLengthRule(255),
            AdaptiveRateCardReservationNameLengthRule(120),
            AdaptiveRateCardReservationCodePrefixRule("REF"),
            AdaptiveRateCardReservationAllowedVehicleIdRule(mutableListOf<String>("capped")),
        ),
    )
}
