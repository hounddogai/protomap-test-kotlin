package com.acme.transport.crewing.swap.policy

/** A rule a swap entry must pass. Check returns its problems, or none. */
interface SwapEntryRule {
    fun check(item: SwapEntry): MutableList<String>
}

data class SwapEntryDiscountRangeRule(var min: Double, var max: Double) : SwapEntryRule {
    override fun check(item: SwapEntry): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapEntryChecksumLengthRule(var maxLength: Int) : SwapEntryRule {
    override fun check(item: SwapEntry): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapEntryCodeLengthRule(var maxLength: Int) : SwapEntryRule {
    override fun check(item: SwapEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapEntryCostRangeRule(var min: Double, var max: Double) : SwapEntryRule {
    override fun check(item: SwapEntry): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a swap entry. */
data class SwapEntryValidator(var rules: MutableList<SwapEntryRule>) {
    fun validate(item: SwapEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SwapEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapEntryValidator(): SwapEntryValidator {
    return SwapEntryValidator(
        mutableListOf<SwapEntryRule>(
            SwapEntryDiscountRangeRule(8.6, 2555.3),
            SwapEntryChecksumLengthRule(80),
            SwapEntryCodeLengthRule(64),
            SwapEntryCostRangeRule(4.8, 2285.2),
        ),
    )
}
