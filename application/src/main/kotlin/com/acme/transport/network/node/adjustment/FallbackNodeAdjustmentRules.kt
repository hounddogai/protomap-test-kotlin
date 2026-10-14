package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a fallback node adjustment must pass. Check returns its problems, or none. */
interface FallbackNodeAdjustmentRule {
    fun check(item: FallbackNodeAdjustment): MutableList<String>
}

data class FallbackNodeAdjustmentRequiredReviewerRule(var message: String) : FallbackNodeAdjustmentRule {
    override fun check(item: FallbackNodeAdjustment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeAdjustmentRequiredLabelRule(var message: String) : FallbackNodeAdjustmentRule {
    override fun check(item: FallbackNodeAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeAdjustmentRequiredCodeRule(var message: String) : FallbackNodeAdjustmentRule {
    override fun check(item: FallbackNodeAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeAdjustmentCapacityRangeRule(var min: Int, var max: Int) : FallbackNodeAdjustmentRule {
    override fun check(item: FallbackNodeAdjustment): MutableList<String> {
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

data class FallbackNodeAdjustmentAllowedLabelRule(
    var allowed: MutableList<String>,
) : FallbackNodeAdjustmentRule {
    override fun check(item: FallbackNodeAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback node adjustment. */
data class FallbackNodeAdjustmentValidator(var rules: MutableList<FallbackNodeAdjustmentRule>) {
    fun validate(item: FallbackNodeAdjustment): MutableList<String> {
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

    fun requireValid(item: FallbackNodeAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackNodeAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackNodeAdjustmentValidator(): FallbackNodeAdjustmentValidator {
    return FallbackNodeAdjustmentValidator(
        mutableListOf<FallbackNodeAdjustmentRule>(
            FallbackNodeAdjustmentRequiredReviewerRule("fallback node adjustment needs an entry"),
            FallbackNodeAdjustmentRequiredLabelRule("fallback node adjustment needs this field"),
            FallbackNodeAdjustmentRequiredCodeRule("fallback node adjustment needs this field"),
            FallbackNodeAdjustmentCapacityRangeRule(1000, 86400),
            FallbackNodeAdjustmentAllowedLabelRule(mutableListOf<String>("regional", "tiered")),
        ),
    )
}
