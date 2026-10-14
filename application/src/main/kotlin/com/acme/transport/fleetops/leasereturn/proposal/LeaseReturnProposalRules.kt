package com.acme.transport.fleetops.leasereturn.proposal

/** A rule a lease return proposal must pass. Check returns its problems, or none. */
interface LeaseReturnProposalRule {
    fun check(item: LeaseReturnProposal): MutableList<String>
}

data class LeaseReturnProposalCreatedAtRangeRule(var min: Int, var max: Int) : LeaseReturnProposalRule {
    override fun check(item: LeaseReturnProposal): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProposalRequiredLabelRule(var message: String) : LeaseReturnProposalRule {
    override fun check(item: LeaseReturnProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProposalCodeLengthRule(var maxLength: Int) : LeaseReturnProposalRule {
    override fun check(item: LeaseReturnProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProposalRequiredCodeRule(var message: String) : LeaseReturnProposalRule {
    override fun check(item: LeaseReturnProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProposalLabelLengthRule(var maxLength: Int) : LeaseReturnProposalRule {
    override fun check(item: LeaseReturnProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return proposal. */
data class LeaseReturnProposalValidator(var rules: MutableList<LeaseReturnProposalRule>) {
    fun validate(item: LeaseReturnProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnProposalValidator(): LeaseReturnProposalValidator {
    return LeaseReturnProposalValidator(
        mutableListOf<LeaseReturnProposalRule>(
            LeaseReturnProposalCreatedAtRangeRule(32, 32),
            LeaseReturnProposalRequiredLabelRule("lease return proposal needs an entry"),
            LeaseReturnProposalCodeLengthRule(3600),
            LeaseReturnProposalRequiredCodeRule("lease return proposal needs this field"),
            LeaseReturnProposalLabelLengthRule(80),
        ),
    )
}
