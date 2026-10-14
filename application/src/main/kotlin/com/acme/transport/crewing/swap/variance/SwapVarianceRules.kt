package com.acme.transport.crewing.swap.variance

/** A rule a swap variance must pass. Check returns its problems, or none. */
interface SwapVarianceRule {
    fun check(item: SwapVariance): MutableList<String>
}

data class SwapVarianceTitleLengthRule(var maxLength: Int) : SwapVarianceRule {
    override fun check(item: SwapVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapVariancePalletsRangeRule(var min: Int, var max: Int) : SwapVarianceRule {
    override fun check(item: SwapVariance): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapVarianceCodeLengthRule(var maxLength: Int) : SwapVarianceRule {
    override fun check(item: SwapVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap variance. */
data class SwapVarianceValidator(var rules: MutableList<SwapVarianceRule>) {
    fun validate(item: SwapVariance): MutableList<String> {
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

    fun requireValid(item: SwapVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapVarianceValidator(): SwapVarianceValidator {
    return SwapVarianceValidator(
        mutableListOf<SwapVarianceRule>(
            SwapVarianceTitleLengthRule(32),
            SwapVariancePalletsRangeRule(80, 3600),
            SwapVarianceCodeLengthRule(1000),
        ),
    )
}
