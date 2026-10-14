package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize

/** A rule a tiered cutoff variance must pass. Check returns its problems, or none. */
interface TieredCutoffVarianceRule {
    fun check(item: TieredCutoffVariance): MutableList<String>
}

data class TieredCutoffVarianceRequiredCurrencyRule(var message: String) : TieredCutoffVarianceRule {
    override fun check(item: TieredCutoffVariance): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffVarianceFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : TieredCutoffVarianceRule {
    override fun check(item: TieredCutoffVariance): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffVarianceAllowedTitleRule(var allowed: MutableList<String>) : TieredCutoffVarianceRule {
    override fun check(item: TieredCutoffVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffVarianceCodePrefixRule(var prefix: String) : TieredCutoffVarianceRule {
    override fun check(item: TieredCutoffVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffVarianceExpiresAtRangeRule(var min: Int, var max: Int) : TieredCutoffVarianceRule {
    override fun check(item: TieredCutoffVariance): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffVarianceRetriesRangeRule(var min: Int, var max: Int) : TieredCutoffVarianceRule {
    override fun check(item: TieredCutoffVariance): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered cutoff variance. */
data class TieredCutoffVarianceValidator(var rules: MutableList<TieredCutoffVarianceRule>) {
    fun validate(item: TieredCutoffVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCutoffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCutoffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCutoffVarianceValidator(): TieredCutoffVarianceValidator {
    return TieredCutoffVarianceValidator(
        mutableListOf<TieredCutoffVarianceRule>(
            TieredCutoffVarianceRequiredCurrencyRule("tiered cutoff variance needs a value"),
            TieredCutoffVarianceFuelLitersRangeRule(7.3, 347.6),
            TieredCutoffVarianceAllowedTitleRule(mutableListOf<String>()),
            TieredCutoffVarianceCodePrefixRule("OPS"),
            TieredCutoffVarianceExpiresAtRangeRule(64, 1000),
            TieredCutoffVarianceRetriesRangeRule(1000, 3600),
        ),
    )
}
