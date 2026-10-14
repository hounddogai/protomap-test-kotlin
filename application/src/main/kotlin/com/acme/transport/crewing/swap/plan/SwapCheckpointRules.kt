package com.acme.transport.crewing.swap.plan

/** A rule a swap checkpoint must pass. Check returns its problems, or none. */
interface SwapCheckpointRule {
    fun check(item: SwapCheckpoint): MutableList<String>
}

data class SwapCheckpointRequiredCodeRule(var message: String) : SwapCheckpointRule {
    override fun check(item: SwapCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SwapCheckpointLocaleLengthRule(var maxLength: Int) : SwapCheckpointRule {
    override fun check(item: SwapCheckpoint): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapCheckpointMarginRangeRule(var min: Double, var max: Double) : SwapCheckpointRule {
    override fun check(item: SwapCheckpoint): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap checkpoint. */
data class SwapCheckpointValidator(var rules: MutableList<SwapCheckpointRule>) {
    fun validate(item: SwapCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapCheckpointValidator(): SwapCheckpointValidator {
    return SwapCheckpointValidator(
        mutableListOf<SwapCheckpointRule>(
            SwapCheckpointRequiredCodeRule("swap checkpoint needs an entry"),
            SwapCheckpointLocaleLengthRule(80),
            SwapCheckpointMarginRangeRule(2.8, 1018.1),
        ),
    )
}
