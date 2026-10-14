package com.acme.transport.terminal.sortation.snapshot

/** A rule a sortation threshold must pass. Check returns its problems, or none. */
interface SortationThresholdRule {
    fun check(item: SortationThreshold): MutableList<String>
}

data class SortationThresholdDockLengthRule(var maxLength: Int) : SortationThresholdRule {
    override fun check(item: SortationThreshold): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationThresholdScoreRangeRule(var min: Double, var max: Double) : SortationThresholdRule {
    override fun check(item: SortationThreshold): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class SortationThresholdCodePrefixRule(var prefix: String) : SortationThresholdRule {
    override fun check(item: SortationThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation threshold. */
data class SortationThresholdValidator(var rules: MutableList<SortationThresholdRule>) {
    fun validate(item: SortationThreshold): MutableList<String> {
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

    fun requireValid(item: SortationThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationThresholdValidator(): SortationThresholdValidator {
    return SortationThresholdValidator(
        mutableListOf<SortationThresholdRule>(
            SortationThresholdDockLengthRule(1000),
            SortationThresholdScoreRangeRule(4.3, 1666.7),
            SortationThresholdCodePrefixRule("OPS"),
        ),
    )
}
