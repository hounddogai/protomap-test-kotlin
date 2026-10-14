package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.normalize

/** A rule a adaptive capacity proposal must pass. Check returns its problems, or none. */
interface AdaptiveCapacityProposalRule {
    fun check(item: AdaptiveCapacityProposal): MutableList<String>
}

data class AdaptiveCapacityProposalRequiredNotesRule(var message: String) : AdaptiveCapacityProposalRule {
    override fun check(item: AdaptiveCapacityProposal): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityProposalCodePrefixRule(var prefix: String) : AdaptiveCapacityProposalRule {
    override fun check(item: AdaptiveCapacityProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityProposalAmountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveCapacityProposalRule {
    override fun check(item: AdaptiveCapacityProposal): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityProposalAllowedRegionRule(
    var allowed: MutableList<String>,
) : AdaptiveCapacityProposalRule {
    override fun check(item: AdaptiveCapacityProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive capacity proposal. */
data class AdaptiveCapacityProposalValidator(var rules: MutableList<AdaptiveCapacityProposalRule>) {
    fun validate(item: AdaptiveCapacityProposal): MutableList<String> {
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

    fun requireValid(item: AdaptiveCapacityProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCapacityProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCapacityProposalValidator(): AdaptiveCapacityProposalValidator {
    return AdaptiveCapacityProposalValidator(
        mutableListOf<AdaptiveCapacityProposalRule>(
            AdaptiveCapacityProposalRequiredNotesRule("adaptive capacity proposal needs an entry"),
            AdaptiveCapacityProposalCodePrefixRule("OPS"),
            AdaptiveCapacityProposalAmountRangeRule(3.1, 1339.7),
            AdaptiveCapacityProposalAllowedRegionRule(mutableListOf<String>("manual", "fallback", "global")),
        ),
    )
}
