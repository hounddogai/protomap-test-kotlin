package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.normalize

/** A rule a fallback clearance policy must pass. Check returns its problems, or none. */
interface FallbackClearancePolicyRule {
    fun check(item: FallbackClearancePolicy): MutableList<String>
}

data class FallbackClearancePolicyRequiredLocaleRule(var message: String) : FallbackClearancePolicyRule {
    override fun check(item: FallbackClearancePolicy): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearancePolicyRequiredCodeRule(var message: String) : FallbackClearancePolicyRule {
    override fun check(item: FallbackClearancePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearancePolicyAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : FallbackClearancePolicyRule {
    override fun check(item: FallbackClearancePolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearancePolicyCodeLengthRule(var maxLength: Int) : FallbackClearancePolicyRule {
    override fun check(item: FallbackClearancePolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackClearancePolicyTrailerIdLengthRule(var maxLength: Int) : FallbackClearancePolicyRule {
    override fun check(item: FallbackClearancePolicy): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback clearance policy. */
data class FallbackClearancePolicyValidator(var rules: MutableList<FallbackClearancePolicyRule>) {
    fun validate(item: FallbackClearancePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackClearancePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackClearancePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackClearancePolicyValidator(): FallbackClearancePolicyValidator {
    return FallbackClearancePolicyValidator(
        mutableListOf<FallbackClearancePolicyRule>(
            FallbackClearancePolicyRequiredLocaleRule("fallback clearance policy needs an entry"),
            FallbackClearancePolicyRequiredCodeRule("fallback clearance policy needs this field"),
            FallbackClearancePolicyAllowedTrailerIdRule(mutableListOf<String>("rolling")),
            FallbackClearancePolicyCodeLengthRule(255),
            FallbackClearancePolicyTrailerIdLengthRule(1000),
        ),
    )
}
