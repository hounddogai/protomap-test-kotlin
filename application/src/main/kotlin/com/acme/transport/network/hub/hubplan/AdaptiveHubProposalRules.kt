package com.acme.transport.network.hub.hubplan

/** A rule a adaptive hub proposal must pass. Check returns its problems, or none. */
interface AdaptiveHubProposalRule {
    fun check(item: AdaptiveHubProposal): MutableList<String>
}

data class AdaptiveHubProposalWeightRangeRule(var min: Double, var max: Double) : AdaptiveHubProposalRule {
    override fun check(item: AdaptiveHubProposal): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubProposalLabelLengthRule(var maxLength: Int) : AdaptiveHubProposalRule {
    override fun check(item: AdaptiveHubProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubProposalUnitLengthRule(var maxLength: Int) : AdaptiveHubProposalRule {
    override fun check(item: AdaptiveHubProposal): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubProposalCodePrefixRule(var prefix: String) : AdaptiveHubProposalRule {
    override fun check(item: AdaptiveHubProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubProposalRequiredCodeRule(var message: String) : AdaptiveHubProposalRule {
    override fun check(item: AdaptiveHubProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubProposalRequiredLabelRule(var message: String) : AdaptiveHubProposalRule {
    override fun check(item: AdaptiveHubProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive hub proposal. */
data class AdaptiveHubProposalValidator(var rules: MutableList<AdaptiveHubProposalRule>) {
    fun validate(item: AdaptiveHubProposal): MutableList<String> {
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

    fun requireValid(item: AdaptiveHubProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveHubProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveHubProposalValidator(): AdaptiveHubProposalValidator {
    return AdaptiveHubProposalValidator(
        mutableListOf<AdaptiveHubProposalRule>(
            AdaptiveHubProposalWeightRangeRule(6.2, 1148.0),
            AdaptiveHubProposalLabelLengthRule(1000),
            AdaptiveHubProposalUnitLengthRule(32),
            AdaptiveHubProposalCodePrefixRule("OPS"),
            AdaptiveHubProposalRequiredCodeRule("adaptive hub proposal needs a value"),
            AdaptiveHubProposalRequiredLabelRule("adaptive hub proposal needs this field"),
        ),
    )
}
