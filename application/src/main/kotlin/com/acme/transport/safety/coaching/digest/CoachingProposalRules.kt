package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.text.normalize

/** A rule a coaching proposal must pass. Check returns its problems, or none. */
interface CoachingProposalRule {
    fun check(item: CoachingProposal): MutableList<String>
}

data class CoachingProposalRequiredNameRule(var message: String) : CoachingProposalRule {
    override fun check(item: CoachingProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingProposalNameLengthRule(var maxLength: Int) : CoachingProposalRule {
    override fun check(item: CoachingProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingProposalRatioRangeRule(var min: Double, var max: Double) : CoachingProposalRule {
    override fun check(item: CoachingProposal): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingProposalAllowedRegionRule(var allowed: MutableList<String>) : CoachingProposalRule {
    override fun check(item: CoachingProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching proposal. */
data class CoachingProposalValidator(var rules: MutableList<CoachingProposalRule>) {
    fun validate(item: CoachingProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingProposalValidator(): CoachingProposalValidator {
    return CoachingProposalValidator(
        mutableListOf<CoachingProposalRule>(
            CoachingProposalRequiredNameRule("coaching proposal needs this field"),
            CoachingProposalNameLengthRule(16),
            CoachingProposalRatioRangeRule(3.1, 2121.3),
            CoachingProposalAllowedRegionRule(mutableListOf<String>("batched", "capped")),
        ),
    )
}
