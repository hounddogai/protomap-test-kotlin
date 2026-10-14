package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a seasonal rest break ledger must pass. Check returns its problems, or none. */
interface SeasonalRestBreakLedgerRule {
    fun check(item: SeasonalRestBreakLedger): MutableList<String>
}

data class SeasonalRestBreakLedgerAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalRestBreakLedgerRule {
    override fun check(item: SeasonalRestBreakLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakLedgerRetriesRangeRule(var min: Int, var max: Int) : SeasonalRestBreakLedgerRule {
    override fun check(item: SeasonalRestBreakLedger): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakLedgerMinimumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRestBreakLedgerRule {
    override fun check(item: SeasonalRestBreakLedger): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakLedgerDescriptionLengthRule(var maxLength: Int) : SeasonalRestBreakLedgerRule {
    override fun check(item: SeasonalRestBreakLedger): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakLedgerLabelLengthRule(var maxLength: Int) : SeasonalRestBreakLedgerRule {
    override fun check(item: SeasonalRestBreakLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRestBreakLedgerMaximumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalRestBreakLedgerRule {
    override fun check(item: SeasonalRestBreakLedger): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal rest break ledger. */
data class SeasonalRestBreakLedgerValidator(var rules: MutableList<SeasonalRestBreakLedgerRule>) {
    fun validate(item: SeasonalRestBreakLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalRestBreakLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRestBreakLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRestBreakLedgerValidator(): SeasonalRestBreakLedgerValidator {
    return SeasonalRestBreakLedgerValidator(
        mutableListOf<SeasonalRestBreakLedgerRule>(
            SeasonalRestBreakLedgerAllowedLabelRule(mutableListOf<String>("fallback")),
            SeasonalRestBreakLedgerRetriesRangeRule(3600, 86400),
            SeasonalRestBreakLedgerMinimumRangeRule(6.7, 2245.2),
            SeasonalRestBreakLedgerDescriptionLengthRule(16),
            SeasonalRestBreakLedgerLabelLengthRule(80),
            SeasonalRestBreakLedgerMaximumRangeRule(1.2, 3684.4),
        ),
    )
}
