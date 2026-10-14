package com.acme.transport.safety.nearmiss.proposal

/** A rule a strict near miss proposal must pass. Check returns its problems, or none. */
interface StrictNearMissProposalRule {
    fun check(item: StrictNearMissProposal): MutableList<String>
}

data class StrictNearMissProposalTitleLengthRule(var maxLength: Int) : StrictNearMissProposalRule {
    override fun check(item: StrictNearMissProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictNearMissProposalRevisionRangeRule(var min: Int, var max: Int) : StrictNearMissProposalRule {
    override fun check(item: StrictNearMissProposal): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class StrictNearMissProposalRequiredCodeRule(var message: String) : StrictNearMissProposalRule {
    override fun check(item: StrictNearMissProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict near miss proposal. */
data class StrictNearMissProposalValidator(var rules: MutableList<StrictNearMissProposalRule>) {
    fun validate(item: StrictNearMissProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictNearMissProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictNearMissProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictNearMissProposalValidator(): StrictNearMissProposalValidator {
    return StrictNearMissProposalValidator(
        mutableListOf<StrictNearMissProposalRule>(
            StrictNearMissProposalTitleLengthRule(3600),
            StrictNearMissProposalRevisionRangeRule(32, 32),
            StrictNearMissProposalRequiredCodeRule("strict near miss proposal needs this field"),
        ),
    )
}
