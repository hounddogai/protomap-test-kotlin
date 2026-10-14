package com.acme.transport.crewing.swap.score

/** A rule a swap score must pass. Check returns its problems, or none. */
interface SwapScoreRule {
    fun check(item: SwapScore): MutableList<String>
}

data class SwapScoreCodeLengthRule(var maxLength: Int) : SwapScoreRule {
    override fun check(item: SwapScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapScoreRequiredTimezoneRule(var message: String) : SwapScoreRule {
    override fun check(item: SwapScore): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapScoreRatioRangeRule(var min: Double, var max: Double) : SwapScoreRule {
    override fun check(item: SwapScore): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class SwapScoreEndsAtRangeRule(var min: Int, var max: Int) : SwapScoreRule {
    override fun check(item: SwapScore): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class SwapScoreCodePrefixRule(var prefix: String) : SwapScoreRule {
    override fun check(item: SwapScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapScoreNameLengthRule(var maxLength: Int) : SwapScoreRule {
    override fun check(item: SwapScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap score. */
data class SwapScoreValidator(var rules: MutableList<SwapScoreRule>) {
    fun validate(item: SwapScore): MutableList<String> {
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

    fun requireValid(item: SwapScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapScore>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapScoreValidator(): SwapScoreValidator {
    return SwapScoreValidator(
        mutableListOf<SwapScoreRule>(
            SwapScoreCodeLengthRule(80),
            SwapScoreRequiredTimezoneRule("swap score needs a value"),
            SwapScoreRatioRangeRule(5.1, 3399.6),
            SwapScoreEndsAtRangeRule(80, 1000),
            SwapScoreCodePrefixRule("Q"),
            SwapScoreNameLengthRule(86400),
        ),
    )
}
