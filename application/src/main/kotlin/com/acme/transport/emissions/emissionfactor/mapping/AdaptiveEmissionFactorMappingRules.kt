package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.text.normalize

/** A rule a adaptive emission factor mapping must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorMappingRule {
    fun check(item: AdaptiveEmissionFactorMapping): MutableList<String>
}

data class AdaptiveEmissionFactorMappingRequiredCarrierRule(
    var message: String,
) : AdaptiveEmissionFactorMappingRule {
    override fun check(item: AdaptiveEmissionFactorMapping): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorMappingCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveEmissionFactorMappingRule {
    override fun check(item: AdaptiveEmissionFactorMapping): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveEmissionFactorMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveEmissionFactorMappingRule {
    override fun check(item: AdaptiveEmissionFactorMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorMappingPhoneLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorMappingRule {
    override fun check(item: AdaptiveEmissionFactorMapping): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorMappingCarrierLengthRule(
    var maxLength: Int,
) : AdaptiveEmissionFactorMappingRule {
    override fun check(item: AdaptiveEmissionFactorMapping): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorMappingRequiredCodeRule(
    var message: String,
) : AdaptiveEmissionFactorMappingRule {
    override fun check(item: AdaptiveEmissionFactorMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor mapping. */
data class AdaptiveEmissionFactorMappingValidator(var rules: MutableList<AdaptiveEmissionFactorMappingRule>) {
    fun validate(item: AdaptiveEmissionFactorMapping): MutableList<String> {
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

    fun requireValid(item: AdaptiveEmissionFactorMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorMappingValidator(): AdaptiveEmissionFactorMappingValidator {
    return AdaptiveEmissionFactorMappingValidator(
        mutableListOf<AdaptiveEmissionFactorMappingRule>(
            AdaptiveEmissionFactorMappingRequiredCarrierRule("adaptive emission factor mapping needs a value"),
            AdaptiveEmissionFactorMappingCreatedAtRangeRule(80, 3600),
            AdaptiveEmissionFactorMappingAllowedCodeRule(mutableListOf<String>("batched")),
            AdaptiveEmissionFactorMappingPhoneLengthRule(32),
            AdaptiveEmissionFactorMappingCarrierLengthRule(255),
            AdaptiveEmissionFactorMappingRequiredCodeRule("adaptive emission factor mapping needs an entry"),
        ),
    )
}
