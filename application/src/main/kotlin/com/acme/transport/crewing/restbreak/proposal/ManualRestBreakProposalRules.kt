package com.acme.transport.crewing.restbreak.proposal

/** A rule a manual rest break proposal must pass. Check returns its problems, or none. */
interface ManualRestBreakProposalRule {
    fun check(item: ManualRestBreakProposal): MutableList<String>
}

data class ManualRestBreakProposalRequiredTitleRule(var message: String) : ManualRestBreakProposalRule {
    override fun check(item: ManualRestBreakProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualRestBreakProposalCodeLengthRule(var maxLength: Int) : ManualRestBreakProposalRule {
    override fun check(item: ManualRestBreakProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualRestBreakProposalDockLengthRule(var maxLength: Int) : ManualRestBreakProposalRule {
    override fun check(item: ManualRestBreakProposal): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual rest break proposal. */
data class ManualRestBreakProposalValidator(var rules: MutableList<ManualRestBreakProposalRule>) {
    fun validate(item: ManualRestBreakProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualRestBreakProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualRestBreakProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultManualRestBreakProposalValidator(): ManualRestBreakProposalValidator {
    return ManualRestBreakProposalValidator(
        mutableListOf<ManualRestBreakProposalRule>(
            ManualRestBreakProposalRequiredTitleRule("manual rest break proposal needs an entry"),
            ManualRestBreakProposalCodeLengthRule(16),
            ManualRestBreakProposalDockLengthRule(1000),
        ),
    )
}
