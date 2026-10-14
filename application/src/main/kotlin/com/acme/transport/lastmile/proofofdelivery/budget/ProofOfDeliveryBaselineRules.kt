package com.acme.transport.lastmile.proofofdelivery.budget

/** A rule a proof of delivery baseline must pass. Check returns its problems, or none. */
interface ProofOfDeliveryBaselineRule {
    fun check(item: ProofOfDeliveryBaseline): MutableList<String>
}

data class ProofOfDeliveryBaselineRequiredCodeRule(var message: String) : ProofOfDeliveryBaselineRule {
    override fun check(item: ProofOfDeliveryBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBaselineLabelLengthRule(var maxLength: Int) : ProofOfDeliveryBaselineRule {
    override fun check(item: ProofOfDeliveryBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBaselineCarrierLengthRule(var maxLength: Int) : ProofOfDeliveryBaselineRule {
    override fun check(item: ProofOfDeliveryBaseline): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBaselineRequiredLabelRule(var message: String) : ProofOfDeliveryBaselineRule {
    override fun check(item: ProofOfDeliveryBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery baseline. */
data class ProofOfDeliveryBaselineValidator(var rules: MutableList<ProofOfDeliveryBaselineRule>) {
    fun validate(item: ProofOfDeliveryBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryBaselineValidator(): ProofOfDeliveryBaselineValidator {
    return ProofOfDeliveryBaselineValidator(
        mutableListOf<ProofOfDeliveryBaselineRule>(
            ProofOfDeliveryBaselineRequiredCodeRule("proof of delivery baseline needs an entry"),
            ProofOfDeliveryBaselineLabelLengthRule(255),
            ProofOfDeliveryBaselineCarrierLengthRule(120),
            ProofOfDeliveryBaselineRequiredLabelRule("proof of delivery baseline needs an entry"),
        ),
    )
}
