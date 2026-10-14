package com.acme.transport.terminal.dockdoor.window

/** A rule a rolling dock door proposal must pass. Check returns its problems, or none. */
interface RollingDockDoorProposalRule {
    fun check(item: RollingDockDoorProposal): MutableList<String>
}

data class RollingDockDoorProposalRequiredCodeRule(var message: String) : RollingDockDoorProposalRule {
    override fun check(item: RollingDockDoorProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorProposalStartsAtRangeRule(var min: Int, var max: Int) : RollingDockDoorProposalRule {
    override fun check(item: RollingDockDoorProposal): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorProposalPinnedGuardRule(var limit: Int) : RollingDockDoorProposalRule {
    override fun check(item: RollingDockDoorProposal): MutableList<String> {
        if (item.pinned && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorProposalNameLengthRule(var maxLength: Int) : RollingDockDoorProposalRule {
    override fun check(item: RollingDockDoorProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDockDoorProposalCurrencyLengthRule(var maxLength: Int) : RollingDockDoorProposalRule {
    override fun check(item: RollingDockDoorProposal): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dock door proposal. */
data class RollingDockDoorProposalValidator(var rules: MutableList<RollingDockDoorProposalRule>) {
    fun validate(item: RollingDockDoorProposal): MutableList<String> {
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

    fun requireValid(item: RollingDockDoorProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDockDoorProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDockDoorProposalValidator(): RollingDockDoorProposalValidator {
    return RollingDockDoorProposalValidator(
        mutableListOf<RollingDockDoorProposalRule>(
            RollingDockDoorProposalRequiredCodeRule("rolling dock door proposal needs a value"),
            RollingDockDoorProposalStartsAtRangeRule(64, 120),
            RollingDockDoorProposalPinnedGuardRule(3600),
            RollingDockDoorProposalNameLengthRule(3600),
            RollingDockDoorProposalCurrencyLengthRule(3600),
        ),
    )
}
