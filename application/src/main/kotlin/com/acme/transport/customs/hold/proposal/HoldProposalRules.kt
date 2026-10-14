package com.acme.transport.customs.hold.proposal

/** A rule a hold proposal must pass. Check returns its problems, or none. */
interface HoldProposalRule {
    fun check(item: HoldProposal): MutableList<String>
}

data class HoldProposalCodePrefixRule(var prefix: String) : HoldProposalRule {
    override fun check(item: HoldProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldProposalCodeLengthRule(var maxLength: Int) : HoldProposalRule {
    override fun check(item: HoldProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldProposalCapacityRangeRule(var min: Int, var max: Int) : HoldProposalRule {
    override fun check(item: HoldProposal): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold proposal. */
data class HoldProposalValidator(var rules: MutableList<HoldProposalRule>) {
    fun validate(item: HoldProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldProposalValidator(): HoldProposalValidator {
    return HoldProposalValidator(
        mutableListOf<HoldProposalRule>(
            HoldProposalCodePrefixRule("X"),
            HoldProposalCodeLengthRule(64),
            HoldProposalCapacityRangeRule(120, 1000),
        ),
    )
}
