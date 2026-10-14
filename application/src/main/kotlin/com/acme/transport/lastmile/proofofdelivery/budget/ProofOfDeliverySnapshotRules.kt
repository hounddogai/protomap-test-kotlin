package com.acme.transport.lastmile.proofofdelivery.budget

/** A rule a proof of delivery snapshot must pass. Check returns its problems, or none. */
interface ProofOfDeliverySnapshotRule {
    fun check(item: ProofOfDeliverySnapshot): MutableList<String>
}

data class ProofOfDeliverySnapshotRequiredRouteCodeRule(var message: String) : ProofOfDeliverySnapshotRule {
    override fun check(item: ProofOfDeliverySnapshot): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliverySnapshotWeightRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliverySnapshotRule {
    override fun check(item: ProofOfDeliverySnapshot): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliverySnapshotExpiresAtRangeRule(var min: Int, var max: Int) : ProofOfDeliverySnapshotRule {
    override fun check(item: ProofOfDeliverySnapshot): MutableList<String> {
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

/** Runs every rule of a proof of delivery snapshot. */
data class ProofOfDeliverySnapshotValidator(var rules: MutableList<ProofOfDeliverySnapshotRule>) {
    fun validate(item: ProofOfDeliverySnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliverySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliverySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliverySnapshotValidator(): ProofOfDeliverySnapshotValidator {
    return ProofOfDeliverySnapshotValidator(
        mutableListOf<ProofOfDeliverySnapshotRule>(
            ProofOfDeliverySnapshotRequiredRouteCodeRule("proof of delivery snapshot needs this field"),
            ProofOfDeliverySnapshotWeightRangeRule(2.6, 428.9),
            ProofOfDeliverySnapshotExpiresAtRangeRule(1000, 3600),
        ),
    )
}
