package com.acme.transport.lastmile.proofofdelivery.score

/** A rule a strict proof of delivery reservation must pass. Check returns its problems, or none. */
interface StrictProofOfDeliveryReservationRule {
    fun check(item: StrictProofOfDeliveryReservation): MutableList<String>
}

data class StrictProofOfDeliveryReservationRequiredCodeRule(
    var message: String,
) : StrictProofOfDeliveryReservationRule {
    override fun check(item: StrictProofOfDeliveryReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictProofOfDeliveryReservationRequiredNameRule(
    var message: String,
) : StrictProofOfDeliveryReservationRule {
    override fun check(item: StrictProofOfDeliveryReservation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictProofOfDeliveryReservationRequiredLabelRule(
    var message: String,
) : StrictProofOfDeliveryReservationRule {
    override fun check(item: StrictProofOfDeliveryReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictProofOfDeliveryReservationCodePrefixRule(
    var prefix: String,
) : StrictProofOfDeliveryReservationRule {
    override fun check(item: StrictProofOfDeliveryReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictProofOfDeliveryReservationLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : StrictProofOfDeliveryReservationRule {
    override fun check(item: StrictProofOfDeliveryReservation): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict proof of delivery reservation. */
data class StrictProofOfDeliveryReservationValidator(
    var rules: MutableList<StrictProofOfDeliveryReservationRule>,
) {
    fun validate(item: StrictProofOfDeliveryReservation): MutableList<String> {
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

    fun requireValid(item: StrictProofOfDeliveryReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictProofOfDeliveryReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictProofOfDeliveryReservationValidator(): StrictProofOfDeliveryReservationValidator {
    return StrictProofOfDeliveryReservationValidator(
        mutableListOf<StrictProofOfDeliveryReservationRule>(
            StrictProofOfDeliveryReservationRequiredCodeRule(
                "strict proof of delivery reservation needs this field",
            ),
            StrictProofOfDeliveryReservationRequiredNameRule(
                "strict proof of delivery reservation needs a value",
            ),
            StrictProofOfDeliveryReservationRequiredLabelRule(
                "strict proof of delivery reservation needs this field",
            ),
            StrictProofOfDeliveryReservationCodePrefixRule("REF"),
            StrictProofOfDeliveryReservationLoadKgRangeRule(4.1, 2233.3),
        ),
    )
}
