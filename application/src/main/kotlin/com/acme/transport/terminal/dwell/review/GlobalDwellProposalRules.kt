package com.acme.transport.terminal.dwell.review

/** A rule a global dwell proposal must pass. Check returns its problems, or none. */
interface GlobalDwellProposalRule {
    fun check(item: GlobalDwellProposal): MutableList<String>
}

data class GlobalDwellProposalCodeLengthRule(var maxLength: Int) : GlobalDwellProposalRule {
    override fun check(item: GlobalDwellProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellProposalCodePrefixRule(var prefix: String) : GlobalDwellProposalRule {
    override fun check(item: GlobalDwellProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellProposalRequiredCodeRule(var message: String) : GlobalDwellProposalRule {
    override fun check(item: GlobalDwellProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dwell proposal. */
data class GlobalDwellProposalValidator(var rules: MutableList<GlobalDwellProposalRule>) {
    fun validate(item: GlobalDwellProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDwellProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDwellProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDwellProposalValidator(): GlobalDwellProposalValidator {
    return GlobalDwellProposalValidator(
        mutableListOf<GlobalDwellProposalRule>(
            GlobalDwellProposalCodeLengthRule(32),
            GlobalDwellProposalCodePrefixRule("Q"),
            GlobalDwellProposalRequiredCodeRule("global dwell proposal needs this field"),
        ),
    )
}
