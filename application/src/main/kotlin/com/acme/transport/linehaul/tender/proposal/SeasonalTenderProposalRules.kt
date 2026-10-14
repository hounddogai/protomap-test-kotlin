package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.text.normalize

/** A rule a seasonal tender proposal must pass. Check returns its problems, or none. */
interface SeasonalTenderProposalRule {
    fun check(item: SeasonalTenderProposal): MutableList<String>
}

data class SeasonalTenderProposalAllowedUnitRule(
    var allowed: MutableList<String>,
) : SeasonalTenderProposalRule {
    override fun check(item: SeasonalTenderProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderProposalCodePrefixRule(var prefix: String) : SeasonalTenderProposalRule {
    override fun check(item: SeasonalTenderProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderProposalRateRangeRule(var min: Double, var max: Double) : SeasonalTenderProposalRule {
    override fun check(item: SeasonalTenderProposal): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderProposalRequiredLabelRule(var message: String) : SeasonalTenderProposalRule {
    override fun check(item: SeasonalTenderProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTenderProposalRequiredDescriptionRule(var message: String) : SeasonalTenderProposalRule {
    override fun check(item: SeasonalTenderProposal): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tender proposal. */
data class SeasonalTenderProposalValidator(var rules: MutableList<SeasonalTenderProposalRule>) {
    fun validate(item: SeasonalTenderProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTenderProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTenderProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTenderProposalValidator(): SeasonalTenderProposalValidator {
    return SeasonalTenderProposalValidator(
        mutableListOf<SeasonalTenderProposalRule>(
            SeasonalTenderProposalAllowedUnitRule(mutableListOf<String>("regional", "global", "weighted")),
            SeasonalTenderProposalCodePrefixRule("OPS"),
            SeasonalTenderProposalRateRangeRule(2.4, 4370.2),
            SeasonalTenderProposalRequiredLabelRule("seasonal tender proposal needs this field"),
            SeasonalTenderProposalRequiredDescriptionRule("seasonal tender proposal needs this field"),
        ),
    )
}
