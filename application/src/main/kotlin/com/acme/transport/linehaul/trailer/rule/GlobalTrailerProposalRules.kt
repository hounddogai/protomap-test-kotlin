package com.acme.transport.linehaul.trailer.rule

/** A rule a global trailer proposal must pass. Check returns its problems, or none. */
interface GlobalTrailerProposalRule {
    fun check(item: GlobalTrailerProposal): MutableList<String>
}

data class GlobalTrailerProposalSequenceRangeRule(var min: Int, var max: Int) : GlobalTrailerProposalRule {
    override fun check(item: GlobalTrailerProposal): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerProposalCodePrefixRule(var prefix: String) : GlobalTrailerProposalRule {
    override fun check(item: GlobalTrailerProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTrailerProposalFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : GlobalTrailerProposalRule {
    override fun check(item: GlobalTrailerProposal): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global trailer proposal. */
data class GlobalTrailerProposalValidator(var rules: MutableList<GlobalTrailerProposalRule>) {
    fun validate(item: GlobalTrailerProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalTrailerProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTrailerProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTrailerProposalValidator(): GlobalTrailerProposalValidator {
    return GlobalTrailerProposalValidator(
        mutableListOf<GlobalTrailerProposalRule>(
            GlobalTrailerProposalSequenceRangeRule(64, 3600),
            GlobalTrailerProposalCodePrefixRule("Q"),
            GlobalTrailerProposalFuelLitersRangeRule(4.1, 845.6),
        ),
    )
}
