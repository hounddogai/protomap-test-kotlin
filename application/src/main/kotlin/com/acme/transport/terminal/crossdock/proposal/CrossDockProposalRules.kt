package com.acme.transport.terminal.crossdock.proposal

/** A rule a cross dock proposal must pass. Check returns its problems, or none. */
interface CrossDockProposalRule {
    fun check(item: CrossDockProposal): MutableList<String>
}

data class CrossDockProposalScoreRangeRule(var min: Double, var max: Double) : CrossDockProposalRule {
    override fun check(item: CrossDockProposal): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class CrossDockProposalChannelLengthRule(var maxLength: Int) : CrossDockProposalRule {
    override fun check(item: CrossDockProposal): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockProposalRequiredNameRule(var message: String) : CrossDockProposalRule {
    override fun check(item: CrossDockProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockProposalCodePrefixRule(var prefix: String) : CrossDockProposalRule {
    override fun check(item: CrossDockProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock proposal. */
data class CrossDockProposalValidator(var rules: MutableList<CrossDockProposalRule>) {
    fun validate(item: CrossDockProposal): MutableList<String> {
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

    fun requireValid(item: CrossDockProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockProposalValidator(): CrossDockProposalValidator {
    return CrossDockProposalValidator(
        mutableListOf<CrossDockProposalRule>(
            CrossDockProposalScoreRangeRule(5.4, 3915.9),
            CrossDockProposalChannelLengthRule(16),
            CrossDockProposalRequiredNameRule("cross dock proposal needs a value"),
            CrossDockProposalCodePrefixRule("Q"),
        ),
    )
}
