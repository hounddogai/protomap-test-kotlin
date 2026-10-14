package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.normalize

/** A rule a seasonal proof of delivery allocation must pass. Check returns its problems, or none. */
interface SeasonalProofOfDeliveryAllocationRule {
    fun check(item: SeasonalProofOfDeliveryAllocation): MutableList<String>
}

data class SeasonalProofOfDeliveryAllocationCarrierLengthRule(
    var maxLength: Int,
) : SeasonalProofOfDeliveryAllocationRule {
    override fun check(item: SeasonalProofOfDeliveryAllocation): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryAllocationRequiredCarrierRule(
    var message: String,
) : SeasonalProofOfDeliveryAllocationRule {
    override fun check(item: SeasonalProofOfDeliveryAllocation): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryAllocationReviewerLengthRule(
    var maxLength: Int,
) : SeasonalProofOfDeliveryAllocationRule {
    override fun check(item: SeasonalProofOfDeliveryAllocation): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalProofOfDeliveryAllocationRatioRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalProofOfDeliveryAllocationRule {
    override fun check(item: SeasonalProofOfDeliveryAllocation): MutableList<String> {
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

data class SeasonalProofOfDeliveryAllocationAllowedCarrierRule(
    var allowed: MutableList<String>,
) : SeasonalProofOfDeliveryAllocationRule {
    override fun check(item: SeasonalProofOfDeliveryAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal proof of delivery allocation. */
data class SeasonalProofOfDeliveryAllocationValidator(
    var rules: MutableList<SeasonalProofOfDeliveryAllocationRule>,
) {
    fun validate(item: SeasonalProofOfDeliveryAllocation): MutableList<String> {
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

    fun requireValid(item: SeasonalProofOfDeliveryAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalProofOfDeliveryAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalProofOfDeliveryAllocationValidator(): SeasonalProofOfDeliveryAllocationValidator {
    return SeasonalProofOfDeliveryAllocationValidator(
        mutableListOf<SeasonalProofOfDeliveryAllocationRule>(
            SeasonalProofOfDeliveryAllocationCarrierLengthRule(255),
            SeasonalProofOfDeliveryAllocationRequiredCarrierRule(
                "seasonal proof of delivery allocation needs this field",
            ),
            SeasonalProofOfDeliveryAllocationReviewerLengthRule(64),
            SeasonalProofOfDeliveryAllocationRatioRangeRule(8.1, 1312.3),
            SeasonalProofOfDeliveryAllocationAllowedCarrierRule(mutableListOf<String>("seasonal", "fallback")),
        ),
    )
}
