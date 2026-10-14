package com.acme.transport.safety.coaching.mapping

/** A rule a regional coaching milestone must pass. Check returns its problems, or none. */
interface RegionalCoachingMilestoneRule {
    fun check(item: RegionalCoachingMilestone): MutableList<String>
}

data class RegionalCoachingMilestoneRequiredNameRule(var message: String) : RegionalCoachingMilestoneRule {
    override fun check(item: RegionalCoachingMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingMilestonePhoneLengthRule(var maxLength: Int) : RegionalCoachingMilestoneRule {
    override fun check(item: RegionalCoachingMilestone): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingMilestoneRequiredSummaryRule(var message: String) : RegionalCoachingMilestoneRule {
    override fun check(item: RegionalCoachingMilestone): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalCoachingMilestonePalletsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalCoachingMilestoneRule {
    override fun check(item: RegionalCoachingMilestone): MutableList<String> {
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

/** Runs every rule of a regional coaching milestone. */
data class RegionalCoachingMilestoneValidator(var rules: MutableList<RegionalCoachingMilestoneRule>) {
    fun validate(item: RegionalCoachingMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalCoachingMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalCoachingMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalCoachingMilestoneValidator(): RegionalCoachingMilestoneValidator {
    return RegionalCoachingMilestoneValidator(
        mutableListOf<RegionalCoachingMilestoneRule>(
            RegionalCoachingMilestoneRequiredNameRule("regional coaching milestone needs a value"),
            RegionalCoachingMilestonePhoneLengthRule(32),
            RegionalCoachingMilestoneRequiredSummaryRule("regional coaching milestone needs this field"),
            RegionalCoachingMilestonePalletsRangeRule(16, 32),
        ),
    )
}
