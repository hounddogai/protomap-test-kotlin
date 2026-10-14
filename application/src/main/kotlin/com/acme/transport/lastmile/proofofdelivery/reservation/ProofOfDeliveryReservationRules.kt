package com.acme.transport.lastmile.proofofdelivery.reservation

/** A rule a proof of delivery reservation must pass. Check returns its problems, or none. */
interface ProofOfDeliveryReservationRule {
    fun check(item: ProofOfDeliveryReservation): MutableList<String>
}

data class ProofOfDeliveryReservationTrailerIdLengthRule(var maxLength: Int) : ProofOfDeliveryReservationRule {
    override fun check(item: ProofOfDeliveryReservation): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryReservationRequiredTrailerIdRule(
    var message: String,
) : ProofOfDeliveryReservationRule {
    override fun check(item: ProofOfDeliveryReservation): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryReservationRequiredCodeRule(var message: String) : ProofOfDeliveryReservationRule {
    override fun check(item: ProofOfDeliveryReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryReservationCodePrefixRule(var prefix: String) : ProofOfDeliveryReservationRule {
    override fun check(item: ProofOfDeliveryReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery reservation. */
data class ProofOfDeliveryReservationValidator(var rules: MutableList<ProofOfDeliveryReservationRule>) {
    fun validate(item: ProofOfDeliveryReservation): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryReservationValidator(): ProofOfDeliveryReservationValidator {
    return ProofOfDeliveryReservationValidator(
        mutableListOf<ProofOfDeliveryReservationRule>(
            ProofOfDeliveryReservationTrailerIdLengthRule(1000),
            ProofOfDeliveryReservationRequiredTrailerIdRule("proof of delivery reservation needs a value"),
            ProofOfDeliveryReservationRequiredCodeRule("proof of delivery reservation needs an entry"),
            ProofOfDeliveryReservationCodePrefixRule("X"),
        ),
    )
}
