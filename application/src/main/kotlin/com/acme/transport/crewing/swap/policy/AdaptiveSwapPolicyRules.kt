package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize

/** A rule a adaptive swap policy must pass. Check returns its problems, or none. */
interface AdaptiveSwapPolicyRule {
    fun check(item: AdaptiveSwapPolicy): MutableList<String>
}

data class AdaptiveSwapPolicyLabelLengthRule(var maxLength: Int) : AdaptiveSwapPolicyRule {
    override fun check(item: AdaptiveSwapPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapPolicyCountryLengthRule(var maxLength: Int) : AdaptiveSwapPolicyRule {
    override fun check(item: AdaptiveSwapPolicy): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapPolicyRequiredLabelRule(var message: String) : AdaptiveSwapPolicyRule {
    override fun check(item: AdaptiveSwapPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapPolicyRequiredCountryRule(var message: String) : AdaptiveSwapPolicyRule {
    override fun check(item: AdaptiveSwapPolicy): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapPolicyCodeLengthRule(var maxLength: Int) : AdaptiveSwapPolicyRule {
    override fun check(item: AdaptiveSwapPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapPolicyAllowedCountryRule(var allowed: MutableList<String>) : AdaptiveSwapPolicyRule {
    override fun check(item: AdaptiveSwapPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive swap policy. */
data class AdaptiveSwapPolicyValidator(var rules: MutableList<AdaptiveSwapPolicyRule>) {
    fun validate(item: AdaptiveSwapPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveSwapPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSwapPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSwapPolicyValidator(): AdaptiveSwapPolicyValidator {
    return AdaptiveSwapPolicyValidator(
        mutableListOf<AdaptiveSwapPolicyRule>(
            AdaptiveSwapPolicyLabelLengthRule(32),
            AdaptiveSwapPolicyCountryLengthRule(255),
            AdaptiveSwapPolicyRequiredLabelRule("adaptive swap policy needs this field"),
            AdaptiveSwapPolicyRequiredCountryRule("adaptive swap policy needs an entry"),
            AdaptiveSwapPolicyCodeLengthRule(16),
            AdaptiveSwapPolicyAllowedCountryRule(mutableListOf<String>("pending", "seasonal")),
        ),
    )
}
