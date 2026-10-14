package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.text.normalize

/** A rule a trailer bundle must pass. Check returns its problems, or none. */
interface TrailerBundleRule {
    fun check(item: TrailerBundle): MutableList<String>
}

data class TrailerBundleStopCountRangeRule(var min: Int, var max: Int) : TrailerBundleRule {
    override fun check(item: TrailerBundle): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBundleAmountRangeRule(var min: Double, var max: Double) : TrailerBundleRule {
    override fun check(item: TrailerBundle): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerBundleAllowedCodeRule(var allowed: MutableList<String>) : TrailerBundleRule {
    override fun check(item: TrailerBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer bundle. */
data class TrailerBundleValidator(var rules: MutableList<TrailerBundleRule>) {
    fun validate(item: TrailerBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerBundleValidator(): TrailerBundleValidator {
    return TrailerBundleValidator(
        mutableListOf<TrailerBundleRule>(
            TrailerBundleStopCountRangeRule(1000, 1000),
            TrailerBundleAmountRangeRule(2.9, 727.0),
            TrailerBundleAllowedCodeRule(mutableListOf<String>("seasonal", "batched")),
        ),
    )
}
