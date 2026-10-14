package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.normalize

/** A rule a sortation tier must pass. Check returns its problems, or none. */
interface SortationTierRule {
    fun check(item: SortationTier): MutableList<String>
}

data class SortationTierStartsAtRangeRule(var min: Int, var max: Int) : SortationTierRule {
    override fun check(item: SortationTier): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class SortationTierAllowedLabelRule(var allowed: MutableList<String>) : SortationTierRule {
    override fun check(item: SortationTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class SortationTierCodePrefixRule(var prefix: String) : SortationTierRule {
    override fun check(item: SortationTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationTierRequiredLabelRule(var message: String) : SortationTierRule {
    override fun check(item: SortationTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationTierLabelLengthRule(var maxLength: Int) : SortationTierRule {
    override fun check(item: SortationTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationTierCodeLengthRule(var maxLength: Int) : SortationTierRule {
    override fun check(item: SortationTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation tier. */
data class SortationTierValidator(var rules: MutableList<SortationTierRule>) {
    fun validate(item: SortationTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SortationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationTierValidator(): SortationTierValidator {
    return SortationTierValidator(
        mutableListOf<SortationTierRule>(
            SortationTierStartsAtRangeRule(3600, 3600),
            SortationTierAllowedLabelRule(mutableListOf<String>("scheduled")),
            SortationTierCodePrefixRule("OPS"),
            SortationTierRequiredLabelRule("sortation tier needs a value"),
            SortationTierLabelLengthRule(1000),
            SortationTierCodeLengthRule(1000),
        ),
    )
}
