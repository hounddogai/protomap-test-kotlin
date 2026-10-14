package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.normalize

/** A rule a tiered swap window must pass. Check returns its problems, or none. */
interface TieredSwapWindowRule {
    fun check(item: TieredSwapWindow): MutableList<String>
}

data class TieredSwapWindowLabelLengthRule(var maxLength: Int) : TieredSwapWindowRule {
    override fun check(item: TieredSwapWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapWindowAllowedCodeRule(var allowed: MutableList<String>) : TieredSwapWindowRule {
    override fun check(item: TieredSwapWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapWindowCodePrefixRule(var prefix: String) : TieredSwapWindowRule {
    override fun check(item: TieredSwapWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredSwapWindowDiscountRangeRule(var min: Double, var max: Double) : TieredSwapWindowRule {
    override fun check(item: TieredSwapWindow): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class TieredSwapWindowStopCountRangeRule(var min: Int, var max: Int) : TieredSwapWindowRule {
    override fun check(item: TieredSwapWindow): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class TieredSwapWindowVersionRangeRule(var min: Int, var max: Int) : TieredSwapWindowRule {
    override fun check(item: TieredSwapWindow): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered swap window. */
data class TieredSwapWindowValidator(var rules: MutableList<TieredSwapWindowRule>) {
    fun validate(item: TieredSwapWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredSwapWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSwapWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSwapWindowValidator(): TieredSwapWindowValidator {
    return TieredSwapWindowValidator(
        mutableListOf<TieredSwapWindowRule>(
            TieredSwapWindowLabelLengthRule(86400),
            TieredSwapWindowAllowedCodeRule(mutableListOf<String>("seasonal")),
            TieredSwapWindowCodePrefixRule("OPS"),
            TieredSwapWindowDiscountRangeRule(7.0, 3375.5),
            TieredSwapWindowStopCountRangeRule(80, 80),
            TieredSwapWindowVersionRangeRule(1000, 3600),
        ),
    )
}
