package com.acme.transport.lastmile.proofofdelivery.baseline

/** A rule a weighted proof of delivery reservation must pass. Check returns its problems, or none. */
interface WeightedProofOfDeliveryReservationRule {
    fun check(item: WeightedProofOfDeliveryReservation): MutableList<String>
}

data class WeightedProofOfDeliveryReservationRequiredLabelRule(
    var message: String,
) : WeightedProofOfDeliveryReservationRule {
    override fun check(item: WeightedProofOfDeliveryReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryReservationRequiredLaneRule(
    var message: String,
) : WeightedProofOfDeliveryReservationRule {
    override fun check(item: WeightedProofOfDeliveryReservation): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryReservationLimitRangeRule(
    var min: Int,
    var max: Int,
) : WeightedProofOfDeliveryReservationRule {
    override fun check(item: WeightedProofOfDeliveryReservation): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted proof of delivery reservation. */
data class WeightedProofOfDeliveryReservationValidator(
    var rules: MutableList<WeightedProofOfDeliveryReservationRule>,
) {
    fun validate(item: WeightedProofOfDeliveryReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedProofOfDeliveryReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedProofOfDeliveryReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedProofOfDeliveryReservationValidator(): WeightedProofOfDeliveryReservationValidator {
    return WeightedProofOfDeliveryReservationValidator(
        mutableListOf<WeightedProofOfDeliveryReservationRule>(
            WeightedProofOfDeliveryReservationRequiredLabelRule(
                "weighted proof of delivery reservation needs a value",
            ),
            WeightedProofOfDeliveryReservationRequiredLaneRule(
                "weighted proof of delivery reservation needs an entry",
            ),
            WeightedProofOfDeliveryReservationLimitRangeRule(16, 32),
        ),
    )
}
