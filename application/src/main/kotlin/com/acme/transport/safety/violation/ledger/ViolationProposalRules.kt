package com.acme.transport.safety.violation.ledger

/** A rule a violation proposal must pass. Check returns its problems, or none. */
interface ViolationProposalRule {
    fun check(item: ViolationProposal): MutableList<String>
}

data class ViolationProposalTitleLengthRule(var maxLength: Int) : ViolationProposalRule {
    override fun check(item: ViolationProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationProposalExpiresAtRangeRule(var min: Int, var max: Int) : ViolationProposalRule {
    override fun check(item: ViolationProposal): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ViolationProposalRequiredPhoneRule(var message: String) : ViolationProposalRule {
    override fun check(item: ViolationProposal): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation proposal. */
data class ViolationProposalValidator(var rules: MutableList<ViolationProposalRule>) {
    fun validate(item: ViolationProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationProposalValidator(): ViolationProposalValidator {
    return ViolationProposalValidator(
        mutableListOf<ViolationProposalRule>(
            ViolationProposalTitleLengthRule(64),
            ViolationProposalExpiresAtRangeRule(255, 86400),
            ViolationProposalRequiredPhoneRule("violation proposal needs an entry"),
        ),
    )
}
