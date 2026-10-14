package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.normalize

/** A rule a emission factor allocation must pass. Check returns its problems, or none. */
interface EmissionFactorAllocationRule {
    fun check(item: EmissionFactorAllocation): MutableList<String>
}

data class EmissionFactorAllocationNameLengthRule(var maxLength: Int) : EmissionFactorAllocationRule {
    override fun check(item: EmissionFactorAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAllocationAllowedCodeRule(
    var allowed: MutableList<String>,
) : EmissionFactorAllocationRule {
    override fun check(item: EmissionFactorAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAllocationExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : EmissionFactorAllocationRule {
    override fun check(item: EmissionFactorAllocation): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorAllocationRequiredCodeRule(var message: String) : EmissionFactorAllocationRule {
    override fun check(item: EmissionFactorAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAllocationRequiredLaneRule(var message: String) : EmissionFactorAllocationRule {
    override fun check(item: EmissionFactorAllocation): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAllocationCodePrefixRule(var prefix: String) : EmissionFactorAllocationRule {
    override fun check(item: EmissionFactorAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor allocation. */
data class EmissionFactorAllocationValidator(var rules: MutableList<EmissionFactorAllocationRule>) {
    fun validate(item: EmissionFactorAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorAllocationValidator(): EmissionFactorAllocationValidator {
    return EmissionFactorAllocationValidator(
        mutableListOf<EmissionFactorAllocationRule>(
            EmissionFactorAllocationNameLengthRule(255),
            EmissionFactorAllocationAllowedCodeRule(mutableListOf<String>("batched", "lenient", "primary")),
            EmissionFactorAllocationExpiresAtRangeRule(1000, 3600),
            EmissionFactorAllocationRequiredCodeRule("emission factor allocation needs an entry"),
            EmissionFactorAllocationRequiredLaneRule("emission factor allocation needs this field"),
            EmissionFactorAllocationCodePrefixRule("OPS"),
        ),
    )
}
