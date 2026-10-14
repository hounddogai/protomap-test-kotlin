package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a tiered sortation summary must pass. Check returns its problems, or none. */
interface TieredSortationSummaryRule {
    fun check(item: TieredSortationSummary): MutableList<String>
}

data class TieredSortationSummaryRequiredCodeRule(var message: String) : TieredSortationSummaryRule {
    override fun check(item: TieredSortationSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationSummaryCodeLengthRule(var maxLength: Int) : TieredSortationSummaryRule {
    override fun check(item: TieredSortationSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationSummaryRetriesRangeRule(var min: Int, var max: Int) : TieredSortationSummaryRule {
    override fun check(item: TieredSortationSummary): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class TieredSortationSummaryAllowedNameRule(
    var allowed: MutableList<String>,
) : TieredSortationSummaryRule {
    override fun check(item: TieredSortationSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationSummaryRequiredLabelRule(var message: String) : TieredSortationSummaryRule {
    override fun check(item: TieredSortationSummary): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationSummaryNameLengthRule(var maxLength: Int) : TieredSortationSummaryRule {
    override fun check(item: TieredSortationSummary): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered sortation summary. */
data class TieredSortationSummaryValidator(var rules: MutableList<TieredSortationSummaryRule>) {
    fun validate(item: TieredSortationSummary): MutableList<String> {
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

    fun requireValid(item: TieredSortationSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationSummaryValidator(): TieredSortationSummaryValidator {
    return TieredSortationSummaryValidator(
        mutableListOf<TieredSortationSummaryRule>(
            TieredSortationSummaryRequiredCodeRule("tiered sortation summary needs a value"),
            TieredSortationSummaryCodeLengthRule(255),
            TieredSortationSummaryRetriesRangeRule(64, 3600),
            TieredSortationSummaryAllowedNameRule(mutableListOf<String>("legacy", "default", "legacy")),
            TieredSortationSummaryRequiredLabelRule("tiered sortation summary needs a value"),
            TieredSortationSummaryNameLengthRule(80),
        ),
    )
}
