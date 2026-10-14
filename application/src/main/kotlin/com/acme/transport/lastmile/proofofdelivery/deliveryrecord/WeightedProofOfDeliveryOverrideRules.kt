package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

/** A rule a weighted proof of delivery override must pass. Check returns its problems, or none. */
interface WeightedProofOfDeliveryOverrideRule {
    fun check(item: WeightedProofOfDeliveryOverride): MutableList<String>
}

data class WeightedProofOfDeliveryOverrideCodePrefixRule(
    var prefix: String,
) : WeightedProofOfDeliveryOverrideRule {
    override fun check(item: WeightedProofOfDeliveryOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryOverrideSummaryLengthRule(
    var maxLength: Int,
) : WeightedProofOfDeliveryOverrideRule {
    override fun check(item: WeightedProofOfDeliveryOverride): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryOverrideRequiredTitleRule(
    var message: String,
) : WeightedProofOfDeliveryOverrideRule {
    override fun check(item: WeightedProofOfDeliveryOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryOverrideCapacityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedProofOfDeliveryOverrideRule {
    override fun check(item: WeightedProofOfDeliveryOverride): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class WeightedProofOfDeliveryOverrideRequiredSummaryRule(
    var message: String,
) : WeightedProofOfDeliveryOverrideRule {
    override fun check(item: WeightedProofOfDeliveryOverride): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted proof of delivery override. */
data class WeightedProofOfDeliveryOverrideValidator(
    var rules: MutableList<WeightedProofOfDeliveryOverrideRule>,
) {
    fun validate(item: WeightedProofOfDeliveryOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedProofOfDeliveryOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedProofOfDeliveryOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedProofOfDeliveryOverrideValidator(): WeightedProofOfDeliveryOverrideValidator {
    return WeightedProofOfDeliveryOverrideValidator(
        mutableListOf<WeightedProofOfDeliveryOverrideRule>(
            WeightedProofOfDeliveryOverrideCodePrefixRule("X"),
            WeightedProofOfDeliveryOverrideSummaryLengthRule(1000),
            WeightedProofOfDeliveryOverrideRequiredTitleRule(
                "weighted proof of delivery override needs this field",
            ),
            WeightedProofOfDeliveryOverrideCapacityRangeRule(64, 80),
            WeightedProofOfDeliveryOverrideRequiredSummaryRule(
                "weighted proof of delivery override needs an entry",
            ),
        ),
    )
}
