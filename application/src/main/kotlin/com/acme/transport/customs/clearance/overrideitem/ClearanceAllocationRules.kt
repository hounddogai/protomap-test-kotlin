package com.acme.transport.customs.clearance.overrideitem

/** A rule a clearance allocation must pass. Check returns its problems, or none. */
interface ClearanceAllocationRule {
    fun check(item: ClearanceAllocation): MutableList<String>
}

data class ClearanceAllocationMarginRangeRule(var min: Double, var max: Double) : ClearanceAllocationRule {
    override fun check(item: ClearanceAllocation): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAllocationCurrencyLengthRule(var maxLength: Int) : ClearanceAllocationRule {
    override fun check(item: ClearanceAllocation): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAllocationCodePrefixRule(var prefix: String) : ClearanceAllocationRule {
    override fun check(item: ClearanceAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance allocation. */
data class ClearanceAllocationValidator(var rules: MutableList<ClearanceAllocationRule>) {
    fun validate(item: ClearanceAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceAllocationValidator(): ClearanceAllocationValidator {
    return ClearanceAllocationValidator(
        mutableListOf<ClearanceAllocationRule>(
            ClearanceAllocationMarginRangeRule(8.4, 2725.1),
            ClearanceAllocationCurrencyLengthRule(80),
            ClearanceAllocationCodePrefixRule("X"),
        ),
    )
}
