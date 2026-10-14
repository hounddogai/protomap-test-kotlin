package com.acme.transport.crewing.swap.rule

/** A rule a weighted swap baseline must pass. Check returns its problems, or none. */
interface WeightedSwapBaselineRule {
    fun check(item: WeightedSwapBaseline): MutableList<String>
}

data class WeightedSwapBaselineAmountRangeRule(var min: Double, var max: Double) : WeightedSwapBaselineRule {
    override fun check(item: WeightedSwapBaseline): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapBaselineRetriesRangeRule(var min: Int, var max: Int) : WeightedSwapBaselineRule {
    override fun check(item: WeightedSwapBaseline): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapBaselineCategoryLengthRule(var maxLength: Int) : WeightedSwapBaselineRule {
    override fun check(item: WeightedSwapBaseline): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapBaselinePhoneLengthRule(var maxLength: Int) : WeightedSwapBaselineRule {
    override fun check(item: WeightedSwapBaseline): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapBaselineCodePrefixRule(var prefix: String) : WeightedSwapBaselineRule {
    override fun check(item: WeightedSwapBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedSwapBaselineRequiredCodeRule(var message: String) : WeightedSwapBaselineRule {
    override fun check(item: WeightedSwapBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted swap baseline. */
data class WeightedSwapBaselineValidator(var rules: MutableList<WeightedSwapBaselineRule>) {
    fun validate(item: WeightedSwapBaseline): MutableList<String> {
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

    fun requireValid(item: WeightedSwapBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedSwapBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedSwapBaselineValidator(): WeightedSwapBaselineValidator {
    return WeightedSwapBaselineValidator(
        mutableListOf<WeightedSwapBaselineRule>(
            WeightedSwapBaselineAmountRangeRule(3.8, 1321.7),
            WeightedSwapBaselineRetriesRangeRule(80, 1000),
            WeightedSwapBaselineCategoryLengthRule(32),
            WeightedSwapBaselinePhoneLengthRule(64),
            WeightedSwapBaselineCodePrefixRule("REF"),
            WeightedSwapBaselineRequiredCodeRule("weighted swap baseline needs this field"),
        ),
    )
}
