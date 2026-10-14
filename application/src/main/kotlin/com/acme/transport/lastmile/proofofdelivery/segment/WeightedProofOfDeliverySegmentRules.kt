package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.text.normalize

/** A rule a weighted proof of delivery segment must pass. Check returns its problems, or none. */
interface WeightedProofOfDeliverySegmentRule {
    fun check(item: WeightedProofOfDeliverySegment): MutableList<String>
}

data class WeightedProofOfDeliverySegmentPriorityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedProofOfDeliverySegmentRule {
    override fun check(item: WeightedProofOfDeliverySegment): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class WeightedProofOfDeliverySegmentRequiredDockRule(
    var message: String,
) : WeightedProofOfDeliverySegmentRule {
    override fun check(item: WeightedProofOfDeliverySegment): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedProofOfDeliverySegmentAllowedTitleRule(
    var allowed: MutableList<String>,
) : WeightedProofOfDeliverySegmentRule {
    override fun check(item: WeightedProofOfDeliverySegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted proof of delivery segment. */
data class WeightedProofOfDeliverySegmentValidator(var rules: MutableList<WeightedProofOfDeliverySegmentRule>) {
    fun validate(item: WeightedProofOfDeliverySegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedProofOfDeliverySegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedProofOfDeliverySegment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedProofOfDeliverySegmentValidator(): WeightedProofOfDeliverySegmentValidator {
    return WeightedProofOfDeliverySegmentValidator(
        mutableListOf<WeightedProofOfDeliverySegmentRule>(
            WeightedProofOfDeliverySegmentPriorityRangeRule(64, 64),
            WeightedProofOfDeliverySegmentRequiredDockRule("weighted proof of delivery segment needs a value"),
            WeightedProofOfDeliverySegmentAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
