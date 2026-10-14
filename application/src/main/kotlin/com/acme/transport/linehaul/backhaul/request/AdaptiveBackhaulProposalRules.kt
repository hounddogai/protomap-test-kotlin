package com.acme.transport.linehaul.backhaul.request

/** A rule a adaptive backhaul proposal must pass. Check returns its problems, or none. */
interface AdaptiveBackhaulProposalRule {
    fun check(item: AdaptiveBackhaulProposal): MutableList<String>
}

data class AdaptiveBackhaulProposalPriorityRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveBackhaulProposalRule {
    override fun check(item: AdaptiveBackhaulProposal): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class AdaptiveBackhaulProposalRequiredTitleRule(var message: String) : AdaptiveBackhaulProposalRule {
    override fun check(item: AdaptiveBackhaulProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBackhaulProposalRatioRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveBackhaulProposalRule {
    override fun check(item: AdaptiveBackhaulProposal): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive backhaul proposal. */
data class AdaptiveBackhaulProposalValidator(var rules: MutableList<AdaptiveBackhaulProposalRule>) {
    fun validate(item: AdaptiveBackhaulProposal): MutableList<String> {
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

    fun requireValid(item: AdaptiveBackhaulProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBackhaulProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBackhaulProposalValidator(): AdaptiveBackhaulProposalValidator {
    return AdaptiveBackhaulProposalValidator(
        mutableListOf<AdaptiveBackhaulProposalRule>(
            AdaptiveBackhaulProposalPriorityRangeRule(16, 120),
            AdaptiveBackhaulProposalRequiredTitleRule("adaptive backhaul proposal needs an entry"),
            AdaptiveBackhaulProposalRatioRangeRule(4.2, 3837.5),
        ),
    )
}
