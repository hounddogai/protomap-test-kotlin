package com.acme.transport.lastmile.proofofdelivery.audit

/** A rule a lenient proof of delivery reservation must pass. Check returns its problems, or none. */
interface LenientProofOfDeliveryReservationRule {
    fun check(item: LenientProofOfDeliveryReservation): MutableList<String>
}

data class LenientProofOfDeliveryReservationCodeLengthRule(
    var maxLength: Int,
) : LenientProofOfDeliveryReservationRule {
    override fun check(item: LenientProofOfDeliveryReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryReservationRequiredRegionRule(
    var message: String,
) : LenientProofOfDeliveryReservationRule {
    override fun check(item: LenientProofOfDeliveryReservation): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryReservationCodePrefixRule(
    var prefix: String,
) : LenientProofOfDeliveryReservationRule {
    override fun check(item: LenientProofOfDeliveryReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientProofOfDeliveryReservationStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientProofOfDeliveryReservationRule {
    override fun check(item: LenientProofOfDeliveryReservation): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient proof of delivery reservation. */
data class LenientProofOfDeliveryReservationValidator(
    var rules: MutableList<LenientProofOfDeliveryReservationRule>,
) {
    fun validate(item: LenientProofOfDeliveryReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientProofOfDeliveryReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientProofOfDeliveryReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientProofOfDeliveryReservationValidator(): LenientProofOfDeliveryReservationValidator {
    return LenientProofOfDeliveryReservationValidator(
        mutableListOf<LenientProofOfDeliveryReservationRule>(
            LenientProofOfDeliveryReservationCodeLengthRule(64),
            LenientProofOfDeliveryReservationRequiredRegionRule(
                "lenient proof of delivery reservation needs a value",
            ),
            LenientProofOfDeliveryReservationCodePrefixRule("X"),
            LenientProofOfDeliveryReservationStartsAtRangeRule(1000, 3600),
        ),
    )
}
