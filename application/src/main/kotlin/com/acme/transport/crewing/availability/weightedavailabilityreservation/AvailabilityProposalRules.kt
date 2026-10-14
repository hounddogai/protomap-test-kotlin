package com.acme.transport.crewing.availability.weightedavailabilityreservation

import com.acme.transport.kit.text.normalize

/** A rule a availability proposal must pass. Check returns its problems, or none. */
interface AvailabilityProposalRule {
    fun check(item: AvailabilityProposal): MutableList<String>
}

data class AvailabilityProposalAllowedCodeRule(var allowed: MutableList<String>) : AvailabilityProposalRule {
    override fun check(item: AvailabilityProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProposalRequiredCountryRule(var message: String) : AvailabilityProposalRule {
    override fun check(item: AvailabilityProposal): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProposalCeilingRangeRule(var min: Double, var max: Double) : AvailabilityProposalRule {
    override fun check(item: AvailabilityProposal): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProposalCodeLengthRule(var maxLength: Int) : AvailabilityProposalRule {
    override fun check(item: AvailabilityProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProposalTitleLengthRule(var maxLength: Int) : AvailabilityProposalRule {
    override fun check(item: AvailabilityProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability proposal. */
data class AvailabilityProposalValidator(var rules: MutableList<AvailabilityProposalRule>) {
    fun validate(item: AvailabilityProposal): MutableList<String> {
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

    fun requireValid(item: AvailabilityProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityProposalValidator(): AvailabilityProposalValidator {
    return AvailabilityProposalValidator(
        mutableListOf<AvailabilityProposalRule>(
            AvailabilityProposalAllowedCodeRule(mutableListOf<String>()),
            AvailabilityProposalRequiredCountryRule("availability proposal needs an entry"),
            AvailabilityProposalCeilingRangeRule(8.1, 261.2),
            AvailabilityProposalCodeLengthRule(64),
            AvailabilityProposalTitleLengthRule(86400),
        ),
    )
}
