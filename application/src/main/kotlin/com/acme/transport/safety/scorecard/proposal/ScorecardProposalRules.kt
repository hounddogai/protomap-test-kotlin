package com.acme.transport.safety.scorecard.proposal

/** A rule a scorecard proposal must pass. Check returns its problems, or none. */
interface ScorecardProposalRule {
    fun check(item: ScorecardProposal): MutableList<String>
}

data class ScorecardProposalRequiredExternalRefRule(var message: String) : ScorecardProposalRule {
    override fun check(item: ScorecardProposal): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardProposalReviewerLengthRule(var maxLength: Int) : ScorecardProposalRule {
    override fun check(item: ScorecardProposal): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardProposalRequiredCodeRule(var message: String) : ScorecardProposalRule {
    override fun check(item: ScorecardProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScorecardProposalCodePrefixRule(var prefix: String) : ScorecardProposalRule {
    override fun check(item: ScorecardProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScorecardProposalCodeLengthRule(var maxLength: Int) : ScorecardProposalRule {
    override fun check(item: ScorecardProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scorecard proposal. */
data class ScorecardProposalValidator(var rules: MutableList<ScorecardProposalRule>) {
    fun validate(item: ScorecardProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScorecardProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScorecardProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultScorecardProposalValidator(): ScorecardProposalValidator {
    return ScorecardProposalValidator(
        mutableListOf<ScorecardProposalRule>(
            ScorecardProposalRequiredExternalRefRule("scorecard proposal needs this field"),
            ScorecardProposalReviewerLengthRule(120),
            ScorecardProposalRequiredCodeRule("scorecard proposal needs an entry"),
            ScorecardProposalCodePrefixRule("OPS"),
            ScorecardProposalCodeLengthRule(1000),
        ),
    )
}
