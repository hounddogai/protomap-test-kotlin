package com.acme.transport.crewing.roster.proposal

import com.acme.transport.kit.text.normalize

/** A rule a global roster proposal must pass. Check returns its problems, or none. */
interface GlobalRosterProposalRule {
    fun check(item: GlobalRosterProposal): MutableList<String>
}

data class GlobalRosterProposalRequiredShiftCodeRule(var message: String) : GlobalRosterProposalRule {
    override fun check(item: GlobalRosterProposal): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterProposalCodeLengthRule(var maxLength: Int) : GlobalRosterProposalRule {
    override fun check(item: GlobalRosterProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterProposalExpiresAtRangeRule(var min: Int, var max: Int) : GlobalRosterProposalRule {
    override fun check(item: GlobalRosterProposal): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterProposalRequiredNameRule(var message: String) : GlobalRosterProposalRule {
    override fun check(item: GlobalRosterProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalRosterProposalRevisionRangeRule(var min: Int, var max: Int) : GlobalRosterProposalRule {
    override fun check(item: GlobalRosterProposal): MutableList<String> {
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

data class GlobalRosterProposalAllowedNotesRule(var allowed: MutableList<String>) : GlobalRosterProposalRule {
    override fun check(item: GlobalRosterProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global roster proposal. */
data class GlobalRosterProposalValidator(var rules: MutableList<GlobalRosterProposalRule>) {
    fun validate(item: GlobalRosterProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalRosterProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRosterProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRosterProposalValidator(): GlobalRosterProposalValidator {
    return GlobalRosterProposalValidator(
        mutableListOf<GlobalRosterProposalRule>(
            GlobalRosterProposalRequiredShiftCodeRule("global roster proposal needs an entry"),
            GlobalRosterProposalCodeLengthRule(80),
            GlobalRosterProposalExpiresAtRangeRule(80, 1000),
            GlobalRosterProposalRequiredNameRule("global roster proposal needs an entry"),
            GlobalRosterProposalRevisionRangeRule(80, 1000),
            GlobalRosterProposalAllowedNotesRule(mutableListOf<String>("legacy")),
        ),
    )
}
