package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a default carbon report milestone must pass. Check returns its problems, or none. */
interface DefaultCarbonReportMilestoneRule {
    fun check(item: DefaultCarbonReportMilestone): MutableList<String>
}

data class DefaultCarbonReportMilestoneAllowedTitleRule(
    var allowed: MutableList<String>,
) : DefaultCarbonReportMilestoneRule {
    override fun check(item: DefaultCarbonReportMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMilestoneCodePrefixRule(var prefix: String) : DefaultCarbonReportMilestoneRule {
    override fun check(item: DefaultCarbonReportMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMilestoneGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : DefaultCarbonReportMilestoneRule {
    override fun check(item: DefaultCarbonReportMilestone): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMilestoneActiveGuardRule(var limit: Int) : DefaultCarbonReportMilestoneRule {
    override fun check(item: DefaultCarbonReportMilestone): MutableList<String> {
        if (item.active && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCarbonReportMilestoneStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : DefaultCarbonReportMilestoneRule {
    override fun check(item: DefaultCarbonReportMilestone): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class DefaultCarbonReportMilestoneDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : DefaultCarbonReportMilestoneRule {
    override fun check(item: DefaultCarbonReportMilestone): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default carbon report milestone. */
data class DefaultCarbonReportMilestoneValidator(var rules: MutableList<DefaultCarbonReportMilestoneRule>) {
    fun validate(item: DefaultCarbonReportMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCarbonReportMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCarbonReportMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCarbonReportMilestoneValidator(): DefaultCarbonReportMilestoneValidator {
    return DefaultCarbonReportMilestoneValidator(
        mutableListOf<DefaultCarbonReportMilestoneRule>(
            DefaultCarbonReportMilestoneAllowedTitleRule(mutableListOf<String>()),
            DefaultCarbonReportMilestoneCodePrefixRule("ACM"),
            DefaultCarbonReportMilestoneGraceDaysRangeRule(80, 86400),
            DefaultCarbonReportMilestoneActiveGuardRule(120),
            DefaultCarbonReportMilestoneStartsAtRangeRule(120, 3600),
            DefaultCarbonReportMilestoneDurationMinutesRangeRule(32, 3600),
        ),
    )
}
