package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.text.normalize

/** A rule a lease return threshold must pass. Check returns its problems, or none. */
interface LeaseReturnThresholdRule {
    fun check(item: LeaseReturnThreshold): MutableList<String>
}

data class LeaseReturnThresholdDiscountRangeRule(var min: Double, var max: Double) : LeaseReturnThresholdRule {
    override fun check(item: LeaseReturnThreshold): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnThresholdRegionLengthRule(var maxLength: Int) : LeaseReturnThresholdRule {
    override fun check(item: LeaseReturnThreshold): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnThresholdRequiredRegionRule(var message: String) : LeaseReturnThresholdRule {
    override fun check(item: LeaseReturnThreshold): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnThresholdAllowedRegionRule(var allowed: MutableList<String>) : LeaseReturnThresholdRule {
    override fun check(item: LeaseReturnThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return threshold. */
data class LeaseReturnThresholdValidator(var rules: MutableList<LeaseReturnThresholdRule>) {
    fun validate(item: LeaseReturnThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnThresholdValidator(): LeaseReturnThresholdValidator {
    return LeaseReturnThresholdValidator(
        mutableListOf<LeaseReturnThresholdRule>(
            LeaseReturnThresholdDiscountRangeRule(5.3, 2653.3),
            LeaseReturnThresholdRegionLengthRule(120),
            LeaseReturnThresholdRequiredRegionRule("lease return threshold needs an entry"),
            LeaseReturnThresholdAllowedRegionRule(mutableListOf<String>("strict", "primary", "strict")),
        ),
    )
}
