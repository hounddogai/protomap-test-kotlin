package com.acme.transport.linehaul.tender.window

/** A rule a adaptive tender milestone must pass. Check returns its problems, or none. */
interface AdaptiveTenderMilestoneRule {
    fun check(item: AdaptiveTenderMilestone): MutableList<String>
}

data class AdaptiveTenderMilestoneWeightRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTenderMilestoneRule {
    override fun check(item: AdaptiveTenderMilestone): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class AdaptiveTenderMilestoneDescriptionLengthRule(var maxLength: Int) : AdaptiveTenderMilestoneRule {
    override fun check(item: AdaptiveTenderMilestone): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderMilestoneApprovedGuardRule(var limit: Double) : AdaptiveTenderMilestoneRule {
    override fun check(item: AdaptiveTenderMilestone): MutableList<String> {
        if (item.approved && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderMilestoneRequiredCodeRule(var message: String) : AdaptiveTenderMilestoneRule {
    override fun check(item: AdaptiveTenderMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderMilestoneRequiredLaneRule(var message: String) : AdaptiveTenderMilestoneRule {
    override fun check(item: AdaptiveTenderMilestone): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderMilestoneRequiredTitleRule(var message: String) : AdaptiveTenderMilestoneRule {
    override fun check(item: AdaptiveTenderMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tender milestone. */
data class AdaptiveTenderMilestoneValidator(var rules: MutableList<AdaptiveTenderMilestoneRule>) {
    fun validate(item: AdaptiveTenderMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTenderMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTenderMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTenderMilestoneValidator(): AdaptiveTenderMilestoneValidator {
    return AdaptiveTenderMilestoneValidator(
        mutableListOf<AdaptiveTenderMilestoneRule>(
            AdaptiveTenderMilestoneWeightRangeRule(7.2, 4142.4),
            AdaptiveTenderMilestoneDescriptionLengthRule(32),
            AdaptiveTenderMilestoneApprovedGuardRule(10.0),
            AdaptiveTenderMilestoneRequiredCodeRule("adaptive tender milestone needs this field"),
            AdaptiveTenderMilestoneRequiredLaneRule("adaptive tender milestone needs a value"),
            AdaptiveTenderMilestoneRequiredTitleRule("adaptive tender milestone needs an entry"),
        ),
    )
}
