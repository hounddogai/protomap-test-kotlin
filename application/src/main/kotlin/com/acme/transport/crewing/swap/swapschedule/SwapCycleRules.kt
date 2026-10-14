package com.acme.transport.crewing.swap.swapschedule

/** A rule a swap cycle must pass. Check returns its problems, or none. */
interface SwapCycleRule {
    fun check(item: SwapCycle): MutableList<String>
}

data class SwapCycleCodeLengthRule(var maxLength: Int) : SwapCycleRule {
    override fun check(item: SwapCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapCyclePalletsRangeRule(var min: Int, var max: Int) : SwapCycleRule {
    override fun check(item: SwapCycle): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class SwapCycleRequiredLabelRule(var message: String) : SwapCycleRule {
    override fun check(item: SwapCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap cycle. */
data class SwapCycleValidator(var rules: MutableList<SwapCycleRule>) {
    fun validate(item: SwapCycle): MutableList<String> {
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

    fun requireValid(item: SwapCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapCycleValidator(): SwapCycleValidator {
    return SwapCycleValidator(
        mutableListOf<SwapCycleRule>(
            SwapCycleCodeLengthRule(3600),
            SwapCyclePalletsRangeRule(16, 80),
            SwapCycleRequiredLabelRule("swap cycle needs an entry"),
        ),
    )
}
