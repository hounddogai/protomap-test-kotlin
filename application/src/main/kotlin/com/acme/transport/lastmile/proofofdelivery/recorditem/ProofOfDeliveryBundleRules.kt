package com.acme.transport.lastmile.proofofdelivery.recorditem

/** A rule a proof of delivery bundle must pass. Check returns its problems, or none. */
interface ProofOfDeliveryBundleRule {
    fun check(item: ProofOfDeliveryBundle): MutableList<String>
}

data class ProofOfDeliveryBundleCodeLengthRule(var maxLength: Int) : ProofOfDeliveryBundleRule {
    override fun check(item: ProofOfDeliveryBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ProofOfDeliveryBundleRatioRangeRule(var min: Double, var max: Double) : ProofOfDeliveryBundleRule {
    override fun check(item: ProofOfDeliveryBundle): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class ProofOfDeliveryBundleDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : ProofOfDeliveryBundleRule {
    override fun check(item: ProofOfDeliveryBundle): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a proof of delivery bundle. */
data class ProofOfDeliveryBundleValidator(var rules: MutableList<ProofOfDeliveryBundleRule>) {
    fun validate(item: ProofOfDeliveryBundle): MutableList<String> {
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

    fun requireValid(item: ProofOfDeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ProofOfDeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultProofOfDeliveryBundleValidator(): ProofOfDeliveryBundleValidator {
    return ProofOfDeliveryBundleValidator(
        mutableListOf<ProofOfDeliveryBundleRule>(
            ProofOfDeliveryBundleCodeLengthRule(255),
            ProofOfDeliveryBundleRatioRangeRule(0.8, 4146.6),
            ProofOfDeliveryBundleDistanceKmRangeRule(8.3, 4826.2),
        ),
    )
}
