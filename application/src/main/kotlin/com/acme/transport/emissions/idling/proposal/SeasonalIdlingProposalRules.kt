package com.acme.transport.emissions.idling.proposal

/** A rule a seasonal idling proposal must pass. Check returns its problems, or none. */
interface SeasonalIdlingProposalRule {
    fun check(item: SeasonalIdlingProposal): MutableList<String>
}

data class SeasonalIdlingProposalVisibleGuardRule(var limit: Double) : SeasonalIdlingProposalRule {
    override fun check(item: SeasonalIdlingProposal): MutableList<String> {
        if (item.visible && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingProposalCodePrefixRule(var prefix: String) : SeasonalIdlingProposalRule {
    override fun check(item: SeasonalIdlingProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingProposalNameLengthRule(var maxLength: Int) : SeasonalIdlingProposalRule {
    override fun check(item: SeasonalIdlingProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal idling proposal. */
data class SeasonalIdlingProposalValidator(var rules: MutableList<SeasonalIdlingProposalRule>) {
    fun validate(item: SeasonalIdlingProposal): MutableList<String> {
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

    fun requireValid(item: SeasonalIdlingProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalIdlingProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalIdlingProposalValidator(): SeasonalIdlingProposalValidator {
    return SeasonalIdlingProposalValidator(
        mutableListOf<SeasonalIdlingProposalRule>(
            SeasonalIdlingProposalVisibleGuardRule(1.6),
            SeasonalIdlingProposalCodePrefixRule("OPS"),
            SeasonalIdlingProposalNameLengthRule(3600),
        ),
    )
}
