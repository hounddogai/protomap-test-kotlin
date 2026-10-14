package com.acme.transport.lastmile.proofofdelivery.estimate

/** A rule a default proof of delivery bundle must pass. Check returns its problems, or none. */
interface DefaultProofOfDeliveryBundleRule {
    fun check(item: DefaultProofOfDeliveryBundle): MutableList<String>
}

data class DefaultProofOfDeliveryBundleCodePrefixRule(var prefix: String) : DefaultProofOfDeliveryBundleRule {
    override fun check(item: DefaultProofOfDeliveryBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryBundleWeightRangeRule(
    var min: Double,
    var max: Double,
) : DefaultProofOfDeliveryBundleRule {
    override fun check(item: DefaultProofOfDeliveryBundle): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryBundleSummaryLengthRule(
    var maxLength: Int,
) : DefaultProofOfDeliveryBundleRule {
    override fun check(item: DefaultProofOfDeliveryBundle): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default proof of delivery bundle. */
data class DefaultProofOfDeliveryBundleValidator(var rules: MutableList<DefaultProofOfDeliveryBundleRule>) {
    fun validate(item: DefaultProofOfDeliveryBundle): MutableList<String> {
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

    fun requireValid(item: DefaultProofOfDeliveryBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultProofOfDeliveryBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultProofOfDeliveryBundleValidator(): DefaultProofOfDeliveryBundleValidator {
    return DefaultProofOfDeliveryBundleValidator(
        mutableListOf<DefaultProofOfDeliveryBundleRule>(
            DefaultProofOfDeliveryBundleCodePrefixRule("X"),
            DefaultProofOfDeliveryBundleWeightRangeRule(4.1, 301.0),
            DefaultProofOfDeliveryBundleSummaryLengthRule(1000),
        ),
    )
}
