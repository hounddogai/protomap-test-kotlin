package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.text.normalize

/** A rule a swap baseline must pass. Check returns its problems, or none. */
interface SwapBaselineRule {
    fun check(item: SwapBaseline): MutableList<String>
}

data class SwapBaselineCostRangeRule(var min: Double, var max: Double) : SwapBaselineRule {
    override fun check(item: SwapBaseline): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapBaselineAllowedLocaleRule(var allowed: MutableList<String>) : SwapBaselineRule {
    override fun check(item: SwapBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class SwapBaselineCodeLengthRule(var maxLength: Int) : SwapBaselineRule {
    override fun check(item: SwapBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap baseline. */
data class SwapBaselineValidator(var rules: MutableList<SwapBaselineRule>) {
    fun validate(item: SwapBaseline): MutableList<String> {
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

    fun requireValid(item: SwapBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapBaselineValidator(): SwapBaselineValidator {
    return SwapBaselineValidator(
        mutableListOf<SwapBaselineRule>(
            SwapBaselineCostRangeRule(2.0, 1357.9),
            SwapBaselineAllowedLocaleRule(mutableListOf<String>("tiered", "primary", "regional")),
            SwapBaselineCodeLengthRule(32),
        ),
    )
}
