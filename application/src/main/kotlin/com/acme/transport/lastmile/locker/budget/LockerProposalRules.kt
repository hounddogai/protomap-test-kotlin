package com.acme.transport.lastmile.locker.budget

/** A rule a locker proposal must pass. Check returns its problems, or none. */
interface LockerProposalRule {
    fun check(item: LockerProposal): MutableList<String>
}

data class LockerProposalCodeLengthRule(var maxLength: Int) : LockerProposalRule {
    override fun check(item: LockerProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerProposalRequiredDepotRule(var message: String) : LockerProposalRule {
    override fun check(item: LockerProposal): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerProposalSequenceRangeRule(var min: Int, var max: Int) : LockerProposalRule {
    override fun check(item: LockerProposal): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker proposal. */
data class LockerProposalValidator(var rules: MutableList<LockerProposalRule>) {
    fun validate(item: LockerProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerProposalValidator(): LockerProposalValidator {
    return LockerProposalValidator(
        mutableListOf<LockerProposalRule>(
            LockerProposalCodeLengthRule(86400),
            LockerProposalRequiredDepotRule("locker proposal needs a value"),
            LockerProposalSequenceRangeRule(32, 120),
        ),
    )
}
