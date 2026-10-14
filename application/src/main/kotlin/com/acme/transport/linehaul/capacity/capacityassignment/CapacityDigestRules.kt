package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.normalize

/** A rule a capacity digest must pass. Check returns its problems, or none. */
interface CapacityDigestRule {
    fun check(item: CapacityDigest): MutableList<String>
}

data class CapacityDigestVersionRangeRule(var min: Int, var max: Int) : CapacityDigestRule {
    override fun check(item: CapacityDigest): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityDigestVisibleGuardRule(var limit: Double) : CapacityDigestRule {
    override fun check(item: CapacityDigest): MutableList<String> {
        if (item.visible && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class CapacityDigestMinimumRangeRule(var min: Double, var max: Double) : CapacityDigestRule {
    override fun check(item: CapacityDigest): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class CapacityDigestCodePrefixRule(var prefix: String) : CapacityDigestRule {
    override fun check(item: CapacityDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityDigestRateRangeRule(var min: Double, var max: Double) : CapacityDigestRule {
    override fun check(item: CapacityDigest): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class CapacityDigestAllowedCodeRule(var allowed: MutableList<String>) : CapacityDigestRule {
    override fun check(item: CapacityDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity digest. */
data class CapacityDigestValidator(var rules: MutableList<CapacityDigestRule>) {
    fun validate(item: CapacityDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityDigestValidator(): CapacityDigestValidator {
    return CapacityDigestValidator(
        mutableListOf<CapacityDigestRule>(
            CapacityDigestVersionRangeRule(64, 1000),
            CapacityDigestVisibleGuardRule(4.1),
            CapacityDigestMinimumRangeRule(6.3, 3252.6),
            CapacityDigestCodePrefixRule("X"),
            CapacityDigestRateRangeRule(9.0, 4900.8),
            CapacityDigestAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
