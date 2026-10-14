package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.text.normalize

/** A rule a capped proof of delivery group must pass. Check returns its problems, or none. */
interface CappedProofOfDeliveryGroupRule {
    fun check(item: CappedProofOfDeliveryGroup): MutableList<String>
}

data class CappedProofOfDeliveryGroupCodePrefixRule(var prefix: String) : CappedProofOfDeliveryGroupRule {
    override fun check(item: CappedProofOfDeliveryGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryGroupWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : CappedProofOfDeliveryGroupRule {
    override fun check(item: CappedProofOfDeliveryGroup): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedProofOfDeliveryGroupAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : CappedProofOfDeliveryGroupRule {
    override fun check(item: CappedProofOfDeliveryGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped proof of delivery group. */
data class CappedProofOfDeliveryGroupValidator(var rules: MutableList<CappedProofOfDeliveryGroupRule>) {
    fun validate(item: CappedProofOfDeliveryGroup): MutableList<String> {
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

    fun requireValid(item: CappedProofOfDeliveryGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedProofOfDeliveryGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedProofOfDeliveryGroupValidator(): CappedProofOfDeliveryGroupValidator {
    return CappedProofOfDeliveryGroupValidator(
        mutableListOf<CappedProofOfDeliveryGroupRule>(
            CappedProofOfDeliveryGroupCodePrefixRule("OPS"),
            CappedProofOfDeliveryGroupWindowDaysRangeRule(120, 120),
            CappedProofOfDeliveryGroupAllowedTrailerIdRule(mutableListOf<String>("scheduled", "seasonal")),
        ),
    )
}
