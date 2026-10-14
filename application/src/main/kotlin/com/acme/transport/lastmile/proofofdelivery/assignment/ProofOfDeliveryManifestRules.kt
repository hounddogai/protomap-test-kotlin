package com.acme.transport.lastmile.proofofdelivery.assignment

/** A rule a proof of delivery manifest must pass. Check returns its problems, or none. */
interface ProofOfDeliveryManifestRule {
    fun check(item: ProofOfDeliveryManifest): MutableList<String>
}

data class ProofOfDeliveryManifestRequiredLabelRule(var message: String) : ProofOfDeliveryManifestRule {
    override fun check(item: ProofOfDeliveryManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryManifestCodePrefixRule(var prefix: String) : ProofOfDeliveryManifestRule {
    override fun check(item: ProofOfDeliveryManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryManifestRatioRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryManifestRule {
    override fun check(item: ProofOfDeliveryManifest): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery manifest. */
data class ProofOfDeliveryManifestValidator(var rules: MutableList<ProofOfDeliveryManifestRule>) {
    fun validate(item: ProofOfDeliveryManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ProofOfDeliveryManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryManifestValidator(): ProofOfDeliveryManifestValidator {
    return ProofOfDeliveryManifestValidator(
        mutableListOf<ProofOfDeliveryManifestRule>(
            ProofOfDeliveryManifestRequiredLabelRule("proof of delivery manifest needs this field"),
            ProofOfDeliveryManifestCodePrefixRule("OPS"),
            ProofOfDeliveryManifestRatioRangeRule(4.8, 1085.8),
        ),
    )
}
