package com.acme.transport.terminal.sortation.snapshot

/** A rule a sortation override must pass. Check returns its problems, or none. */
interface SortationOverrideRule {
    fun check(item: SortationOverride): MutableList<String>
}

data class SortationOverrideCarrierLengthRule(var maxLength: Int) : SortationOverrideRule {
    override fun check(item: SortationOverride): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationOverrideMarginRangeRule(var min: Double, var max: Double) : SortationOverrideRule {
    override fun check(item: SortationOverride): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationOverrideCodePrefixRule(var prefix: String) : SortationOverrideRule {
    override fun check(item: SortationOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationOverrideCodeLengthRule(var maxLength: Int) : SortationOverrideRule {
    override fun check(item: SortationOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation override. */
data class SortationOverrideValidator(var rules: MutableList<SortationOverrideRule>) {
    fun validate(item: SortationOverride): MutableList<String> {
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

    fun requireValid(item: SortationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationOverrideValidator(): SortationOverrideValidator {
    return SortationOverrideValidator(
        mutableListOf<SortationOverrideRule>(
            SortationOverrideCarrierLengthRule(86400),
            SortationOverrideMarginRangeRule(8.2, 3029.8),
            SortationOverrideCodePrefixRule("Q"),
            SortationOverrideCodeLengthRule(120),
        ),
    )
}
