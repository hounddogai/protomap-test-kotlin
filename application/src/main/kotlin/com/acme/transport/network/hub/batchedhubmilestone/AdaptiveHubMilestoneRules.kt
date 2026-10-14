package com.acme.transport.network.hub.batchedhubmilestone

/** A rule a adaptive hub milestone must pass. Check returns its problems, or none. */
interface AdaptiveHubMilestoneRule {
    fun check(item: AdaptiveHubMilestone): MutableList<String>
}

data class AdaptiveHubMilestoneMaximumRangeRule(var min: Double, var max: Double) : AdaptiveHubMilestoneRule {
    override fun check(item: AdaptiveHubMilestone): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHubMilestonePalletsRangeRule(var min: Int, var max: Int) : AdaptiveHubMilestoneRule {
    override fun check(item: AdaptiveHubMilestone): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class AdaptiveHubMilestoneLabelLengthRule(var maxLength: Int) : AdaptiveHubMilestoneRule {
    override fun check(item: AdaptiveHubMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive hub milestone. */
data class AdaptiveHubMilestoneValidator(var rules: MutableList<AdaptiveHubMilestoneRule>) {
    fun validate(item: AdaptiveHubMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveHubMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveHubMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveHubMilestoneValidator(): AdaptiveHubMilestoneValidator {
    return AdaptiveHubMilestoneValidator(
        mutableListOf<AdaptiveHubMilestoneRule>(
            AdaptiveHubMilestoneMaximumRangeRule(0.9, 4598.1),
            AdaptiveHubMilestonePalletsRangeRule(64, 1000),
            AdaptiveHubMilestoneLabelLengthRule(3600),
        ),
    )
}
