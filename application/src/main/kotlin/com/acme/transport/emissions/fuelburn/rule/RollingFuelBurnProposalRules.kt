package com.acme.transport.emissions.fuelburn.rule

/** A rule a rolling fuel burn proposal must pass. Check returns its problems, or none. */
interface RollingFuelBurnProposalRule {
    fun check(item: RollingFuelBurnProposal): MutableList<String>
}

data class RollingFuelBurnProposalCodePrefixRule(var prefix: String) : RollingFuelBurnProposalRule {
    override fun check(item: RollingFuelBurnProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnProposalBatchSizeRangeRule(var min: Int, var max: Int) : RollingFuelBurnProposalRule {
    override fun check(item: RollingFuelBurnProposal): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnProposalRequiredNameRule(var message: String) : RollingFuelBurnProposalRule {
    override fun check(item: RollingFuelBurnProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnProposalSequenceRangeRule(var min: Int, var max: Int) : RollingFuelBurnProposalRule {
    override fun check(item: RollingFuelBurnProposal): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a rolling fuel burn proposal. */
data class RollingFuelBurnProposalValidator(var rules: MutableList<RollingFuelBurnProposalRule>) {
    fun validate(item: RollingFuelBurnProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnProposalValidator(): RollingFuelBurnProposalValidator {
    return RollingFuelBurnProposalValidator(
        mutableListOf<RollingFuelBurnProposalRule>(
            RollingFuelBurnProposalCodePrefixRule("X"),
            RollingFuelBurnProposalBatchSizeRangeRule(255, 86400),
            RollingFuelBurnProposalRequiredNameRule("rolling fuel burn proposal needs this field"),
            RollingFuelBurnProposalSequenceRangeRule(255, 1000),
        ),
    )
}
