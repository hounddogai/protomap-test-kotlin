package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.text.normalize

/** A rule a adaptive emission factor group must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorGroupRule {
    fun check(item: AdaptiveEmissionFactorGroup): MutableList<String>
}

data class AdaptiveEmissionFactorGroupAmountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveEmissionFactorGroupRule {
    override fun check(item: AdaptiveEmissionFactorGroup): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorGroupAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveEmissionFactorGroupRule {
    override fun check(item: AdaptiveEmissionFactorGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorGroupCodePrefixRule(var prefix: String) : AdaptiveEmissionFactorGroupRule {
    override fun check(item: AdaptiveEmissionFactorGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor group. */
data class AdaptiveEmissionFactorGroupValidator(var rules: MutableList<AdaptiveEmissionFactorGroupRule>) {
    fun validate(item: AdaptiveEmissionFactorGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveEmissionFactorGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorGroupValidator(): AdaptiveEmissionFactorGroupValidator {
    return AdaptiveEmissionFactorGroupValidator(
        mutableListOf<AdaptiveEmissionFactorGroupRule>(
            AdaptiveEmissionFactorGroupAmountRangeRule(8.8, 564.1),
            AdaptiveEmissionFactorGroupAllowedCodeRule(mutableListOf<String>("scheduled")),
            AdaptiveEmissionFactorGroupCodePrefixRule("REF"),
        ),
    )
}
