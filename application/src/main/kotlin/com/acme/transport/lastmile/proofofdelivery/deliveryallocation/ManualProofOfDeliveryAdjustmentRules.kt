package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.normalize

/** A rule a manual proof of delivery adjustment must pass. Check returns its problems, or none. */
interface ManualProofOfDeliveryAdjustmentRule {
    fun check(item: ManualProofOfDeliveryAdjustment): MutableList<String>
}

data class ManualProofOfDeliveryAdjustmentWeightRangeRule(
    var min: Double,
    var max: Double,
) : ManualProofOfDeliveryAdjustmentRule {
    override fun check(item: ManualProofOfDeliveryAdjustment): MutableList<String> {
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

data class ManualProofOfDeliveryAdjustmentAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : ManualProofOfDeliveryAdjustmentRule {
    override fun check(item: ManualProofOfDeliveryAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryAdjustmentVersionRangeRule(
    var min: Int,
    var max: Int,
) : ManualProofOfDeliveryAdjustmentRule {
    override fun check(item: ManualProofOfDeliveryAdjustment): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualProofOfDeliveryAdjustmentRequiredTimezoneRule(
    var message: String,
) : ManualProofOfDeliveryAdjustmentRule {
    override fun check(item: ManualProofOfDeliveryAdjustment): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual proof of delivery adjustment. */
data class ManualProofOfDeliveryAdjustmentValidator(
    var rules: MutableList<ManualProofOfDeliveryAdjustmentRule>,
) {
    fun validate(item: ManualProofOfDeliveryAdjustment): MutableList<String> {
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

    fun requireValid(item: ManualProofOfDeliveryAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualProofOfDeliveryAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualProofOfDeliveryAdjustmentValidator(): ManualProofOfDeliveryAdjustmentValidator {
    return ManualProofOfDeliveryAdjustmentValidator(
        mutableListOf<ManualProofOfDeliveryAdjustmentRule>(
            ManualProofOfDeliveryAdjustmentWeightRangeRule(9.7, 4890.8),
            ManualProofOfDeliveryAdjustmentAllowedTimezoneRule(mutableListOf<String>("manual", "rolling")),
            ManualProofOfDeliveryAdjustmentVersionRangeRule(80, 3600),
            ManualProofOfDeliveryAdjustmentRequiredTimezoneRule(
                "manual proof of delivery adjustment needs this field",
            ),
        ),
    )
}
