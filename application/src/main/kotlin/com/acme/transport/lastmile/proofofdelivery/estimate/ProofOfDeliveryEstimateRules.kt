package com.acme.transport.lastmile.proofofdelivery.estimate

/** A rule a proof of delivery estimate must pass. Check returns its problems, or none. */
interface ProofOfDeliveryEstimateRule {
    fun check(item: ProofOfDeliveryEstimate): MutableList<String>
}

data class ProofOfDeliveryEstimateRequiredCodeRule(var message: String) : ProofOfDeliveryEstimateRule {
    override fun check(item: ProofOfDeliveryEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryEstimateDepotLengthRule(var maxLength: Int) : ProofOfDeliveryEstimateRule {
    override fun check(item: ProofOfDeliveryEstimate): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryEstimateRequiredLabelRule(var message: String) : ProofOfDeliveryEstimateRule {
    override fun check(item: ProofOfDeliveryEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery estimate. */
data class ProofOfDeliveryEstimateValidator(var rules: MutableList<ProofOfDeliveryEstimateRule>) {
    fun validate(item: ProofOfDeliveryEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryEstimateValidator(): ProofOfDeliveryEstimateValidator {
    return ProofOfDeliveryEstimateValidator(
        mutableListOf<ProofOfDeliveryEstimateRule>(
            ProofOfDeliveryEstimateRequiredCodeRule("proof of delivery estimate needs this field"),
            ProofOfDeliveryEstimateDepotLengthRule(86400),
            ProofOfDeliveryEstimateRequiredLabelRule("proof of delivery estimate needs an entry"),
        ),
    )
}
