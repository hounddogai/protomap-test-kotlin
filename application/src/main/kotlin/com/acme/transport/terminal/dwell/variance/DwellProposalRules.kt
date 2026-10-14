package com.acme.transport.terminal.dwell.variance

import com.acme.transport.kit.text.normalize

/** A rule a dwell proposal must pass. Check returns its problems, or none. */
interface DwellProposalRule {
    fun check(item: DwellProposal): MutableList<String>
}

data class DwellProposalRequiredCodeRule(var message: String) : DwellProposalRule {
    override fun check(item: DwellProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellProposalCodeLengthRule(var maxLength: Int) : DwellProposalRule {
    override fun check(item: DwellProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellProposalRequiredContactEmailRule(var message: String) : DwellProposalRule {
    override fun check(item: DwellProposal): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellProposalAllowedContactEmailRule(var allowed: MutableList<String>) : DwellProposalRule {
    override fun check(item: DwellProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DwellProposalFuelLitersRangeRule(var min: Double, var max: Double) : DwellProposalRule {
    override fun check(item: DwellProposal): MutableList<String> {
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

data class DwellProposalCodePrefixRule(var prefix: String) : DwellProposalRule {
    override fun check(item: DwellProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell proposal. */
data class DwellProposalValidator(var rules: MutableList<DwellProposalRule>) {
    fun validate(item: DwellProposal): MutableList<String> {
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

    fun requireValid(item: DwellProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellProposalValidator(): DwellProposalValidator {
    return DwellProposalValidator(
        mutableListOf<DwellProposalRule>(
            DwellProposalRequiredCodeRule("dwell proposal needs an entry"),
            DwellProposalCodeLengthRule(80),
            DwellProposalRequiredContactEmailRule("dwell proposal needs this field"),
            DwellProposalAllowedContactEmailRule(mutableListOf<String>("batched", "regional", "scheduled")),
            DwellProposalFuelLitersRangeRule(1.4, 660.3),
            DwellProposalCodePrefixRule("Q"),
        ),
    )
}
