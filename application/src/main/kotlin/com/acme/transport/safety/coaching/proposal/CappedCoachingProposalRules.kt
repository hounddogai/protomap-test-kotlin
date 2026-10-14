package com.acme.transport.safety.coaching.proposal

/** A rule a capped coaching proposal must pass. Check returns its problems, or none. */
interface CappedCoachingProposalRule {
    fun check(item: CappedCoachingProposal): MutableList<String>
}

data class CappedCoachingProposalRequiredLabelRule(var message: String) : CappedCoachingProposalRule {
    override fun check(item: CappedCoachingProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingProposalDriverIdLengthRule(var maxLength: Int) : CappedCoachingProposalRule {
    override fun check(item: CappedCoachingProposal): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingProposalApprovedGuardRule(var limit: Int) : CappedCoachingProposalRule {
    override fun check(item: CappedCoachingProposal): MutableList<String> {
        if (item.approved && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped coaching proposal. */
data class CappedCoachingProposalValidator(var rules: MutableList<CappedCoachingProposalRule>) {
    fun validate(item: CappedCoachingProposal): MutableList<String> {
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

    fun requireValid(item: CappedCoachingProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCoachingProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCoachingProposalValidator(): CappedCoachingProposalValidator {
    return CappedCoachingProposalValidator(
        mutableListOf<CappedCoachingProposalRule>(
            CappedCoachingProposalRequiredLabelRule("capped coaching proposal needs this field"),
            CappedCoachingProposalDriverIdLengthRule(80),
            CappedCoachingProposalApprovedGuardRule(120),
        ),
    )
}
