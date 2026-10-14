package com.acme.transport.customs.hold.estimate

import com.acme.transport.kit.text.normalize

/** A rule a tiered hold estimate must pass. Check returns its problems, or none. */
interface TieredHoldEstimateRule {
    fun check(item: TieredHoldEstimate): MutableList<String>
}

data class TieredHoldEstimateAllowedRouteCodeRule(var allowed: MutableList<String>) : TieredHoldEstimateRule {
    override fun check(item: TieredHoldEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class TieredHoldEstimateSequenceRangeRule(var min: Int, var max: Int) : TieredHoldEstimateRule {
    override fun check(item: TieredHoldEstimate): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredHoldEstimateRequiredExternalRefRule(var message: String) : TieredHoldEstimateRule {
    override fun check(item: TieredHoldEstimate): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered hold estimate. */
data class TieredHoldEstimateValidator(var rules: MutableList<TieredHoldEstimateRule>) {
    fun validate(item: TieredHoldEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredHoldEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredHoldEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredHoldEstimateValidator(): TieredHoldEstimateValidator {
    return TieredHoldEstimateValidator(
        mutableListOf<TieredHoldEstimateRule>(
            TieredHoldEstimateAllowedRouteCodeRule(mutableListOf<String>("manual", "primary", "fallback")),
            TieredHoldEstimateSequenceRangeRule(80, 255),
            TieredHoldEstimateRequiredExternalRefRule("tiered hold estimate needs a value"),
        ),
    )
}
