package com.acme.transport.crewing.swap.assignment

/** A rule a adaptive swap allocation must pass. Check returns its problems, or none. */
interface AdaptiveSwapAllocationRule {
    fun check(item: AdaptiveSwapAllocation): MutableList<String>
}

data class AdaptiveSwapAllocationQuantityRangeRule(var min: Int, var max: Int) : AdaptiveSwapAllocationRule {
    override fun check(item: AdaptiveSwapAllocation): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapAllocationLabelLengthRule(var maxLength: Int) : AdaptiveSwapAllocationRule {
    override fun check(item: AdaptiveSwapAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapAllocationRequiredCountryRule(var message: String) : AdaptiveSwapAllocationRule {
    override fun check(item: AdaptiveSwapAllocation): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive swap allocation. */
data class AdaptiveSwapAllocationValidator(var rules: MutableList<AdaptiveSwapAllocationRule>) {
    fun validate(item: AdaptiveSwapAllocation): MutableList<String> {
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

    fun requireValid(item: AdaptiveSwapAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSwapAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSwapAllocationValidator(): AdaptiveSwapAllocationValidator {
    return AdaptiveSwapAllocationValidator(
        mutableListOf<AdaptiveSwapAllocationRule>(
            AdaptiveSwapAllocationQuantityRangeRule(64, 64),
            AdaptiveSwapAllocationLabelLengthRule(32),
            AdaptiveSwapAllocationRequiredCountryRule("adaptive swap allocation needs an entry"),
        ),
    )
}
