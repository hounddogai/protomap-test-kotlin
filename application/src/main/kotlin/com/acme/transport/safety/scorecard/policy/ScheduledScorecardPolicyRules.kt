package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.normalize

/** A rule a scheduled scorecard policy must pass. Check returns its problems, or none. */
interface ScheduledScorecardPolicyRule {
    fun check(item: ScheduledScorecardPolicy): MutableList<String>
}

data class ScheduledScorecardPolicyRequiredDepotRule(var message: String) : ScheduledScorecardPolicyRule {
    override fun check(item: ScheduledScorecardPolicy): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardPolicyTitleLengthRule(var maxLength: Int) : ScheduledScorecardPolicyRule {
    override fun check(item: ScheduledScorecardPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardPolicyAllowedTitleRule(
    var allowed: MutableList<String>,
) : ScheduledScorecardPolicyRule {
    override fun check(item: ScheduledScorecardPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledScorecardPolicyRateRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledScorecardPolicyRule {
    override fun check(item: ScheduledScorecardPolicy): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class ScheduledScorecardPolicyBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledScorecardPolicyRule {
    override fun check(item: ScheduledScorecardPolicy): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a scheduled scorecard policy. */
data class ScheduledScorecardPolicyValidator(var rules: MutableList<ScheduledScorecardPolicyRule>) {
    fun validate(item: ScheduledScorecardPolicy): MutableList<String> {
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

    fun requireValid(item: ScheduledScorecardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledScorecardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledScorecardPolicyValidator(): ScheduledScorecardPolicyValidator {
    return ScheduledScorecardPolicyValidator(
        mutableListOf<ScheduledScorecardPolicyRule>(
            ScheduledScorecardPolicyRequiredDepotRule("scheduled scorecard policy needs a value"),
            ScheduledScorecardPolicyTitleLengthRule(1000),
            ScheduledScorecardPolicyAllowedTitleRule(mutableListOf<String>("lenient", "global", "manual")),
            ScheduledScorecardPolicyRateRangeRule(3.0, 664.3),
            ScheduledScorecardPolicyBudgetRangeRule(0.3, 2475.2),
        ),
    )
}
