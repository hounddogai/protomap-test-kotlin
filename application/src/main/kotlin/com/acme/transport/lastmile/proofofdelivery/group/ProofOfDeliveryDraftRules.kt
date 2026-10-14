package com.acme.transport.lastmile.proofofdelivery.group

/** A rule a proof of delivery draft must pass. Check returns its problems, or none. */
interface ProofOfDeliveryDraftRule {
    fun check(item: ProofOfDeliveryDraft): MutableList<String>
}

data class ProofOfDeliveryDraftFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryDraftRule {
    override fun check(item: ProofOfDeliveryDraft): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliveryDraftRequiredRouteCodeRule(var message: String) : ProofOfDeliveryDraftRule {
    override fun check(item: ProofOfDeliveryDraft): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryDraftApprovedGuardRule(var limit: Double) : ProofOfDeliveryDraftRule {
    override fun check(item: ProofOfDeliveryDraft): MutableList<String> {
        if (item.approved && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery draft. */
data class ProofOfDeliveryDraftValidator(var rules: MutableList<ProofOfDeliveryDraftRule>) {
    fun validate(item: ProofOfDeliveryDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryDraftValidator(): ProofOfDeliveryDraftValidator {
    return ProofOfDeliveryDraftValidator(
        mutableListOf<ProofOfDeliveryDraftRule>(
            ProofOfDeliveryDraftFuelLitersRangeRule(1.7, 1875.4),
            ProofOfDeliveryDraftRequiredRouteCodeRule("proof of delivery draft needs an entry"),
            ProofOfDeliveryDraftApprovedGuardRule(5.8),
        ),
    )
}
