package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.kit.text.normalize

/** A rule a global lease return proposal must pass. Check returns its problems, or none. */
interface GlobalLeaseReturnProposalRule {
    fun check(item: GlobalLeaseReturnProposal): MutableList<String>
}

data class GlobalLeaseReturnProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalLeaseReturnProposalRule {
    override fun check(item: GlobalLeaseReturnProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnProposalLimitRangeRule(var min: Int, var max: Int) : GlobalLeaseReturnProposalRule {
    override fun check(item: GlobalLeaseReturnProposal): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnProposalNameLengthRule(var maxLength: Int) : GlobalLeaseReturnProposalRule {
    override fun check(item: GlobalLeaseReturnProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnProposalRequiredNameRule(var message: String) : GlobalLeaseReturnProposalRule {
    override fun check(item: GlobalLeaseReturnProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalLeaseReturnProposalRequiredLabelRule(var message: String) : GlobalLeaseReturnProposalRule {
    override fun check(item: GlobalLeaseReturnProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global lease return proposal. */
data class GlobalLeaseReturnProposalValidator(var rules: MutableList<GlobalLeaseReturnProposalRule>) {
    fun validate(item: GlobalLeaseReturnProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalLeaseReturnProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalLeaseReturnProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalLeaseReturnProposalValidator(): GlobalLeaseReturnProposalValidator {
    return GlobalLeaseReturnProposalValidator(
        mutableListOf<GlobalLeaseReturnProposalRule>(
            GlobalLeaseReturnProposalAllowedCodeRule(mutableListOf<String>("primary")),
            GlobalLeaseReturnProposalLimitRangeRule(16, 255),
            GlobalLeaseReturnProposalNameLengthRule(80),
            GlobalLeaseReturnProposalRequiredNameRule("global lease return proposal needs a value"),
            GlobalLeaseReturnProposalRequiredLabelRule("global lease return proposal needs an entry"),
        ),
    )
}
