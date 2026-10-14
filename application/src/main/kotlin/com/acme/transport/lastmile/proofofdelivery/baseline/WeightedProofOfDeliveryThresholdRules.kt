package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.kit.text.normalize

/** A rule a weighted proof of delivery threshold must pass. Check returns its problems, or none. */
interface WeightedProofOfDeliveryThresholdRule {
    fun check(item: WeightedProofOfDeliveryThreshold): MutableList<String>
}

data class WeightedProofOfDeliveryThresholdAllowedUnitRule(
    var allowed: MutableList<String>,
) : WeightedProofOfDeliveryThresholdRule {
    override fun check(item: WeightedProofOfDeliveryThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is expired")
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryThresholdOwnerIdLengthRule(
    var maxLength: Int,
) : WeightedProofOfDeliveryThresholdRule {
    override fun check(item: WeightedProofOfDeliveryThreshold): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryThresholdApprovedGuardRule(
    var limit: Int,
) : WeightedProofOfDeliveryThresholdRule {
    override fun check(item: WeightedProofOfDeliveryThreshold): MutableList<String> {
        if (item.approved && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliveryThresholdRequiredTitleRule(
    var message: String,
) : WeightedProofOfDeliveryThresholdRule {
    override fun check(item: WeightedProofOfDeliveryThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted proof of delivery threshold. */
data class WeightedProofOfDeliveryThresholdValidator(
    var rules: MutableList<WeightedProofOfDeliveryThresholdRule>,
) {
    fun validate(item: WeightedProofOfDeliveryThreshold): MutableList<String> {
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

    fun requireValid(item: WeightedProofOfDeliveryThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedProofOfDeliveryThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedProofOfDeliveryThresholdValidator(): WeightedProofOfDeliveryThresholdValidator {
    return WeightedProofOfDeliveryThresholdValidator(
        mutableListOf<WeightedProofOfDeliveryThresholdRule>(
            WeightedProofOfDeliveryThresholdAllowedUnitRule(mutableListOf<String>("capped")),
            WeightedProofOfDeliveryThresholdOwnerIdLengthRule(3600),
            WeightedProofOfDeliveryThresholdApprovedGuardRule(64),
            WeightedProofOfDeliveryThresholdRequiredTitleRule(
                "weighted proof of delivery threshold needs an entry",
            ),
        ),
    )
}
