package com.acme.transport.crewing.swap.swapschedule

/** A rule a fallback swap policy must pass. Check returns its problems, or none. */
interface FallbackSwapPolicyRule {
    fun check(item: FallbackSwapPolicy): MutableList<String>
}

data class FallbackSwapPolicyLabelLengthRule(var maxLength: Int) : FallbackSwapPolicyRule {
    override fun check(item: FallbackSwapPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapPolicyThresholdRangeRule(var min: Double, var max: Double) : FallbackSwapPolicyRule {
    override fun check(item: FallbackSwapPolicy): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapPolicyCodeLengthRule(var maxLength: Int) : FallbackSwapPolicyRule {
    override fun check(item: FallbackSwapPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback swap policy. */
data class FallbackSwapPolicyValidator(var rules: MutableList<FallbackSwapPolicyRule>) {
    fun validate(item: FallbackSwapPolicy): MutableList<String> {
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

    fun requireValid(item: FallbackSwapPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackSwapPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackSwapPolicyValidator(): FallbackSwapPolicyValidator {
    return FallbackSwapPolicyValidator(
        mutableListOf<FallbackSwapPolicyRule>(
            FallbackSwapPolicyLabelLengthRule(64),
            FallbackSwapPolicyThresholdRangeRule(5.5, 2236.8),
            FallbackSwapPolicyCodeLengthRule(1000),
        ),
    )
}
