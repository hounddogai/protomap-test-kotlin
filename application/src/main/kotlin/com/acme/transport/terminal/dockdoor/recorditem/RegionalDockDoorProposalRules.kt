package com.acme.transport.terminal.dockdoor.recorditem

/** A rule a regional dock door proposal must pass. Check returns its problems, or none. */
interface RegionalDockDoorProposalRule {
    fun check(item: RegionalDockDoorProposal): MutableList<String>
}

data class RegionalDockDoorProposalNameLengthRule(var maxLength: Int) : RegionalDockDoorProposalRule {
    override fun check(item: RegionalDockDoorProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorProposalChannelLengthRule(var maxLength: Int) : RegionalDockDoorProposalRule {
    override fun check(item: RegionalDockDoorProposal): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorProposalPriorityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalDockDoorProposalRule {
    override fun check(item: RegionalDockDoorProposal): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class RegionalDockDoorProposalRequiredCodeRule(var message: String) : RegionalDockDoorProposalRule {
    override fun check(item: RegionalDockDoorProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dock door proposal. */
data class RegionalDockDoorProposalValidator(var rules: MutableList<RegionalDockDoorProposalRule>) {
    fun validate(item: RegionalDockDoorProposal): MutableList<String> {
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

    fun requireValid(item: RegionalDockDoorProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDockDoorProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDockDoorProposalValidator(): RegionalDockDoorProposalValidator {
    return RegionalDockDoorProposalValidator(
        mutableListOf<RegionalDockDoorProposalRule>(
            RegionalDockDoorProposalNameLengthRule(80),
            RegionalDockDoorProposalChannelLengthRule(80),
            RegionalDockDoorProposalPriorityRangeRule(32, 120),
            RegionalDockDoorProposalRequiredCodeRule("regional dock door proposal needs a value"),
        ),
    )
}
