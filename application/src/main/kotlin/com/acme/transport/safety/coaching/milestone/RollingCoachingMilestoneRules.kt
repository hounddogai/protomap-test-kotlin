package com.acme.transport.safety.coaching.milestone

/** A rule a rolling coaching milestone must pass. Check returns its problems, or none. */
interface RollingCoachingMilestoneRule {
    fun check(item: RollingCoachingMilestone): MutableList<String>
}

data class RollingCoachingMilestoneRequiredCodeRule(var message: String) : RollingCoachingMilestoneRule {
    override fun check(item: RollingCoachingMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingMilestoneRequiredCurrencyRule(var message: String) : RollingCoachingMilestoneRule {
    override fun check(item: RollingCoachingMilestone): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingMilestoneCurrencyLengthRule(var maxLength: Int) : RollingCoachingMilestoneRule {
    override fun check(item: RollingCoachingMilestone): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingMilestoneTitleLengthRule(var maxLength: Int) : RollingCoachingMilestoneRule {
    override fun check(item: RollingCoachingMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingMilestoneRegionLengthRule(var maxLength: Int) : RollingCoachingMilestoneRule {
    override fun check(item: RollingCoachingMilestone): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling coaching milestone. */
data class RollingCoachingMilestoneValidator(var rules: MutableList<RollingCoachingMilestoneRule>) {
    fun validate(item: RollingCoachingMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCoachingMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCoachingMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCoachingMilestoneValidator(): RollingCoachingMilestoneValidator {
    return RollingCoachingMilestoneValidator(
        mutableListOf<RollingCoachingMilestoneRule>(
            RollingCoachingMilestoneRequiredCodeRule("rolling coaching milestone needs an entry"),
            RollingCoachingMilestoneRequiredCurrencyRule("rolling coaching milestone needs this field"),
            RollingCoachingMilestoneCurrencyLengthRule(32),
            RollingCoachingMilestoneTitleLengthRule(3600),
            RollingCoachingMilestoneRegionLengthRule(3600),
        ),
    )
}
