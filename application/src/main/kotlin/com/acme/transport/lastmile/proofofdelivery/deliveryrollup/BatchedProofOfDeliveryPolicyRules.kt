package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.text.normalize

/** A rule a batched proof of delivery policy must pass. Check returns its problems, or none. */
interface BatchedProofOfDeliveryPolicyRule {
    fun check(item: BatchedProofOfDeliveryPolicy): MutableList<String>
}

data class BatchedProofOfDeliveryPolicyCostRangeRule(
    var min: Double,
    var max: Double,
) : BatchedProofOfDeliveryPolicyRule {
    override fun check(item: BatchedProofOfDeliveryPolicy): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class BatchedProofOfDeliveryPolicyTitleLengthRule(var maxLength: Int) : BatchedProofOfDeliveryPolicyRule {
    override fun check(item: BatchedProofOfDeliveryPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryPolicyCodeLengthRule(var maxLength: Int) : BatchedProofOfDeliveryPolicyRule {
    override fun check(item: BatchedProofOfDeliveryPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryPolicyRequiredDepotRule(
    var message: String,
) : BatchedProofOfDeliveryPolicyRule {
    override fun check(item: BatchedProofOfDeliveryPolicy): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryPolicyRequiredTitleRule(
    var message: String,
) : BatchedProofOfDeliveryPolicyRule {
    override fun check(item: BatchedProofOfDeliveryPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryPolicyAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedProofOfDeliveryPolicyRule {
    override fun check(item: BatchedProofOfDeliveryPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched proof of delivery policy. */
data class BatchedProofOfDeliveryPolicyValidator(var rules: MutableList<BatchedProofOfDeliveryPolicyRule>) {
    fun validate(item: BatchedProofOfDeliveryPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedProofOfDeliveryPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedProofOfDeliveryPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedProofOfDeliveryPolicyValidator(): BatchedProofOfDeliveryPolicyValidator {
    return BatchedProofOfDeliveryPolicyValidator(
        mutableListOf<BatchedProofOfDeliveryPolicyRule>(
            BatchedProofOfDeliveryPolicyCostRangeRule(6.6, 672.9),
            BatchedProofOfDeliveryPolicyTitleLengthRule(1000),
            BatchedProofOfDeliveryPolicyCodeLengthRule(16),
            BatchedProofOfDeliveryPolicyRequiredDepotRule("batched proof of delivery policy needs an entry"),
            BatchedProofOfDeliveryPolicyRequiredTitleRule("batched proof of delivery policy needs an entry"),
            BatchedProofOfDeliveryPolicyAllowedCodeRule(mutableListOf<String>("adaptive", "adaptive")),
        ),
    )
}
