package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.kit.text.normalize

/** A rule a fallback ev charge mapping must pass. Check returns its problems, or none. */
interface FallbackEvChargeMappingRule {
    fun check(item: FallbackEvChargeMapping): MutableList<String>
}

data class FallbackEvChargeMappingAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackEvChargeMappingRule {
    override fun check(item: FallbackEvChargeMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeMappingCodePrefixRule(var prefix: String) : FallbackEvChargeMappingRule {
    override fun check(item: FallbackEvChargeMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeMappingCurrencyLengthRule(var maxLength: Int) : FallbackEvChargeMappingRule {
    override fun check(item: FallbackEvChargeMapping): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackEvChargeMappingRegionLengthRule(var maxLength: Int) : FallbackEvChargeMappingRule {
    override fun check(item: FallbackEvChargeMapping): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback ev charge mapping. */
data class FallbackEvChargeMappingValidator(var rules: MutableList<FallbackEvChargeMappingRule>) {
    fun validate(item: FallbackEvChargeMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackEvChargeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackEvChargeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackEvChargeMappingValidator(): FallbackEvChargeMappingValidator {
    return FallbackEvChargeMappingValidator(
        mutableListOf<FallbackEvChargeMappingRule>(
            FallbackEvChargeMappingAllowedCodeRule(mutableListOf<String>("regional", "legacy")),
            FallbackEvChargeMappingCodePrefixRule("REF"),
            FallbackEvChargeMappingCurrencyLengthRule(1000),
            FallbackEvChargeMappingRegionLengthRule(32),
        ),
    )
}
