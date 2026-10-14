package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.text.normalize

/** A rule a seasonal parcel proposal must pass. Check returns its problems, or none. */
interface SeasonalParcelProposalRule {
    fun check(item: SeasonalParcelProposal): MutableList<String>
}

data class SeasonalParcelProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalParcelProposalRule {
    override fun check(item: SeasonalParcelProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelProposalEndsAtRangeRule(var min: Int, var max: Int) : SeasonalParcelProposalRule {
    override fun check(item: SeasonalParcelProposal): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class SeasonalParcelProposalCodeLengthRule(var maxLength: Int) : SeasonalParcelProposalRule {
    override fun check(item: SeasonalParcelProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelProposalGraceDaysRangeRule(var min: Int, var max: Int) : SeasonalParcelProposalRule {
    override fun check(item: SeasonalParcelProposal): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class SeasonalParcelProposalRequiredNameRule(var message: String) : SeasonalParcelProposalRule {
    override fun check(item: SeasonalParcelProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalParcelProposalRequiredCurrencyRule(var message: String) : SeasonalParcelProposalRule {
    override fun check(item: SeasonalParcelProposal): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal parcel proposal. */
data class SeasonalParcelProposalValidator(var rules: MutableList<SeasonalParcelProposalRule>) {
    fun validate(item: SeasonalParcelProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalParcelProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalParcelProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalParcelProposalValidator(): SeasonalParcelProposalValidator {
    return SeasonalParcelProposalValidator(
        mutableListOf<SeasonalParcelProposalRule>(
            SeasonalParcelProposalAllowedCodeRule(mutableListOf<String>("manual", "rolling")),
            SeasonalParcelProposalEndsAtRangeRule(1000, 86400),
            SeasonalParcelProposalCodeLengthRule(86400),
            SeasonalParcelProposalGraceDaysRangeRule(3600, 86400),
            SeasonalParcelProposalRequiredNameRule("seasonal parcel proposal needs a value"),
            SeasonalParcelProposalRequiredCurrencyRule("seasonal parcel proposal needs this field"),
        ),
    )
}
