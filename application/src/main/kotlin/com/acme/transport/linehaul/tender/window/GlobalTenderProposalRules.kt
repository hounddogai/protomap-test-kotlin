package com.acme.transport.linehaul.tender.window

/** A rule a global tender proposal must pass. Check returns its problems, or none. */
interface GlobalTenderProposalRule {
    fun check(item: GlobalTenderProposal): MutableList<String>
}

data class GlobalTenderProposalRequiredLabelRule(var message: String) : GlobalTenderProposalRule {
    override fun check(item: GlobalTenderProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderProposalNameLengthRule(var maxLength: Int) : GlobalTenderProposalRule {
    override fun check(item: GlobalTenderProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTenderProposalCapacityRangeRule(var min: Int, var max: Int) : GlobalTenderProposalRule {
    override fun check(item: GlobalTenderProposal): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global tender proposal. */
data class GlobalTenderProposalValidator(var rules: MutableList<GlobalTenderProposalRule>) {
    fun validate(item: GlobalTenderProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTenderProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTenderProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTenderProposalValidator(): GlobalTenderProposalValidator {
    return GlobalTenderProposalValidator(
        mutableListOf<GlobalTenderProposalRule>(
            GlobalTenderProposalRequiredLabelRule("global tender proposal needs a value"),
            GlobalTenderProposalNameLengthRule(80),
            GlobalTenderProposalCapacityRangeRule(120, 86400),
        ),
    )
}
