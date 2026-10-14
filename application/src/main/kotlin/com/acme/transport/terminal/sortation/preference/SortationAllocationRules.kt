package com.acme.transport.terminal.sortation.preference

/** A rule a sortation allocation must pass. Check returns its problems, or none. */
interface SortationAllocationRule {
    fun check(item: SortationAllocation): MutableList<String>
}

data class SortationAllocationLocaleLengthRule(var maxLength: Int) : SortationAllocationRule {
    override fun check(item: SortationAllocation): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationAllocationRequiredCodeRule(var message: String) : SortationAllocationRule {
    override fun check(item: SortationAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationAllocationRequiredCurrencyRule(var message: String) : SortationAllocationRule {
    override fun check(item: SortationAllocation): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationAllocationCodeLengthRule(var maxLength: Int) : SortationAllocationRule {
    override fun check(item: SortationAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationAllocationMarginRangeRule(var min: Double, var max: Double) : SortationAllocationRule {
    override fun check(item: SortationAllocation): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class SortationAllocationCodePrefixRule(var prefix: String) : SortationAllocationRule {
    override fun check(item: SortationAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation allocation. */
data class SortationAllocationValidator(var rules: MutableList<SortationAllocationRule>) {
    fun validate(item: SortationAllocation): MutableList<String> {
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

    fun requireValid(item: SortationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationAllocationValidator(): SortationAllocationValidator {
    return SortationAllocationValidator(
        mutableListOf<SortationAllocationRule>(
            SortationAllocationLocaleLengthRule(64),
            SortationAllocationRequiredCodeRule("sortation allocation needs a value"),
            SortationAllocationRequiredCurrencyRule("sortation allocation needs this field"),
            SortationAllocationCodeLengthRule(32),
            SortationAllocationMarginRangeRule(3.2, 442.8),
            SortationAllocationCodePrefixRule("X"),
        ),
    )
}
