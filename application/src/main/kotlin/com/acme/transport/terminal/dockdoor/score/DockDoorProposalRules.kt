package com.acme.transport.terminal.dockdoor.score

/** A rule a dock door proposal must pass. Check returns its problems, or none. */
interface DockDoorProposalRule {
    fun check(item: DockDoorProposal): MutableList<String>
}

data class DockDoorProposalRequiredLaneRule(var message: String) : DockDoorProposalRule {
    override fun check(item: DockDoorProposal): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorProposalRateRangeRule(var min: Double, var max: Double) : DockDoorProposalRule {
    override fun check(item: DockDoorProposal): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class DockDoorProposalLaneLengthRule(var maxLength: Int) : DockDoorProposalRule {
    override fun check(item: DockDoorProposal): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorProposalCodeLengthRule(var maxLength: Int) : DockDoorProposalRule {
    override fun check(item: DockDoorProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorProposalTitleLengthRule(var maxLength: Int) : DockDoorProposalRule {
    override fun check(item: DockDoorProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door proposal. */
data class DockDoorProposalValidator(var rules: MutableList<DockDoorProposalRule>) {
    fun validate(item: DockDoorProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorProposalValidator(): DockDoorProposalValidator {
    return DockDoorProposalValidator(
        mutableListOf<DockDoorProposalRule>(
            DockDoorProposalRequiredLaneRule("dock door proposal needs a value"),
            DockDoorProposalRateRangeRule(2.4, 2011.0),
            DockDoorProposalLaneLengthRule(255),
            DockDoorProposalCodeLengthRule(64),
            DockDoorProposalTitleLengthRule(80),
        ),
    )
}
