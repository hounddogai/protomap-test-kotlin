package com.acme.transport.crewing.roster.summary

/** A rule a roster proposal must pass. Check returns its problems, or none. */
interface RosterProposalRule {
    fun check(item: RosterProposal): MutableList<String>
}

data class RosterProposalCodePrefixRule(var prefix: String) : RosterProposalRule {
    override fun check(item: RosterProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RosterProposalGraceDaysRangeRule(var min: Int, var max: Int) : RosterProposalRule {
    override fun check(item: RosterProposal): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterProposalRequiredDockRule(var message: String) : RosterProposalRule {
    override fun check(item: RosterProposal): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RosterProposalTitleLengthRule(var maxLength: Int) : RosterProposalRule {
    override fun check(item: RosterProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster proposal. */
data class RosterProposalValidator(var rules: MutableList<RosterProposalRule>) {
    fun validate(item: RosterProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RosterProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterProposalValidator(): RosterProposalValidator {
    return RosterProposalValidator(
        mutableListOf<RosterProposalRule>(
            RosterProposalCodePrefixRule("X"),
            RosterProposalGraceDaysRangeRule(16, 86400),
            RosterProposalRequiredDockRule("roster proposal needs this field"),
            RosterProposalTitleLengthRule(1000),
        ),
    )
}
