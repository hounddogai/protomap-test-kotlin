package com.acme.transport.lastmile.proofofdelivery.estimate

/** A rule a adaptive proof of delivery threshold must pass. Check returns its problems, or none. */
interface AdaptiveProofOfDeliveryThresholdRule {
    fun check(item: AdaptiveProofOfDeliveryThreshold): MutableList<String>
}

data class AdaptiveProofOfDeliveryThresholdWeightRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveProofOfDeliveryThresholdRule {
    override fun check(item: AdaptiveProofOfDeliveryThreshold): MutableList<String> {
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

data class AdaptiveProofOfDeliveryThresholdCodePrefixRule(
    var prefix: String,
) : AdaptiveProofOfDeliveryThresholdRule {
    override fun check(item: AdaptiveProofOfDeliveryThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryThresholdDescriptionLengthRule(
    var maxLength: Int,
) : AdaptiveProofOfDeliveryThresholdRule {
    override fun check(item: AdaptiveProofOfDeliveryThreshold): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryThresholdRequiredDescriptionRule(
    var message: String,
) : AdaptiveProofOfDeliveryThresholdRule {
    override fun check(item: AdaptiveProofOfDeliveryThreshold): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryThresholdRequiredCodeRule(
    var message: String,
) : AdaptiveProofOfDeliveryThresholdRule {
    override fun check(item: AdaptiveProofOfDeliveryThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveProofOfDeliveryThresholdExternalRefLengthRule(
    var maxLength: Int,
) : AdaptiveProofOfDeliveryThresholdRule {
    override fun check(item: AdaptiveProofOfDeliveryThreshold): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive proof of delivery threshold. */
data class AdaptiveProofOfDeliveryThresholdValidator(
    var rules: MutableList<AdaptiveProofOfDeliveryThresholdRule>,
) {
    fun validate(item: AdaptiveProofOfDeliveryThreshold): MutableList<String> {
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

    fun requireValid(item: AdaptiveProofOfDeliveryThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveProofOfDeliveryThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveProofOfDeliveryThresholdValidator(): AdaptiveProofOfDeliveryThresholdValidator {
    return AdaptiveProofOfDeliveryThresholdValidator(
        mutableListOf<AdaptiveProofOfDeliveryThresholdRule>(
            AdaptiveProofOfDeliveryThresholdWeightRangeRule(4.8, 986.0),
            AdaptiveProofOfDeliveryThresholdCodePrefixRule("Q"),
            AdaptiveProofOfDeliveryThresholdDescriptionLengthRule(32),
            AdaptiveProofOfDeliveryThresholdRequiredDescriptionRule(
                "adaptive proof of delivery threshold needs a value",
            ),
            AdaptiveProofOfDeliveryThresholdRequiredCodeRule(
                "adaptive proof of delivery threshold needs a value",
            ),
            AdaptiveProofOfDeliveryThresholdExternalRefLengthRule(64),
        ),
    )
}
