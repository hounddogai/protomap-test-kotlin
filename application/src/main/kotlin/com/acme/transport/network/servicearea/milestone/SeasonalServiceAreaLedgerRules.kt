package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.text.normalize

/** A rule a seasonal service area ledger must pass. Check returns its problems, or none. */
interface SeasonalServiceAreaLedgerRule {
    fun check(item: SeasonalServiceAreaLedger): MutableList<String>
}

data class SeasonalServiceAreaLedgerAllowedReviewerRule(
    var allowed: MutableList<String>,
) : SeasonalServiceAreaLedgerRule {
    override fun check(item: SeasonalServiceAreaLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaLedgerRequiredCodeRule(var message: String) : SeasonalServiceAreaLedgerRule {
    override fun check(item: SeasonalServiceAreaLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaLedgerPriorityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServiceAreaLedgerRule {
    override fun check(item: SeasonalServiceAreaLedger): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaLedgerCodeLengthRule(var maxLength: Int) : SeasonalServiceAreaLedgerRule {
    override fun check(item: SeasonalServiceAreaLedger): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServiceAreaLedgerLabelLengthRule(var maxLength: Int) : SeasonalServiceAreaLedgerRule {
    override fun check(item: SeasonalServiceAreaLedger): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service area ledger. */
data class SeasonalServiceAreaLedgerValidator(var rules: MutableList<SeasonalServiceAreaLedgerRule>) {
    fun validate(item: SeasonalServiceAreaLedger): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalServiceAreaLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServiceAreaLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServiceAreaLedgerValidator(): SeasonalServiceAreaLedgerValidator {
    return SeasonalServiceAreaLedgerValidator(
        mutableListOf<SeasonalServiceAreaLedgerRule>(
            SeasonalServiceAreaLedgerAllowedReviewerRule(mutableListOf<String>("batched", "strict", "legacy")),
            SeasonalServiceAreaLedgerRequiredCodeRule("seasonal service area ledger needs this field"),
            SeasonalServiceAreaLedgerPriorityRangeRule(1000, 3600),
            SeasonalServiceAreaLedgerCodeLengthRule(16),
            SeasonalServiceAreaLedgerLabelLengthRule(64),
        ),
    )
}
