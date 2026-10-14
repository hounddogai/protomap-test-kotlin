package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.text.normalize

/** A rule a lenient hub plan must pass. Check returns its problems, or none. */
interface LenientHubPlanRule {
    fun check(item: LenientHubPlan): MutableList<String>
}

data class LenientHubPlanRequiredTitleRule(var message: String) : LenientHubPlanRule {
    override fun check(item: LenientHubPlan): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientHubPlanDurationMinutesRangeRule(var min: Int, var max: Int) : LenientHubPlanRule {
    override fun check(item: LenientHubPlan): MutableList<String> {
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

data class LenientHubPlanMaximumRangeRule(var min: Double, var max: Double) : LenientHubPlanRule {
    override fun check(item: LenientHubPlan): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class LenientHubPlanCodePrefixRule(var prefix: String) : LenientHubPlanRule {
    override fun check(item: LenientHubPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientHubPlanAllowedTitleRule(var allowed: MutableList<String>) : LenientHubPlanRule {
    override fun check(item: LenientHubPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient hub plan. */
data class LenientHubPlanValidator(var rules: MutableList<LenientHubPlanRule>) {
    fun validate(item: LenientHubPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientHubPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientHubPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientHubPlanValidator(): LenientHubPlanValidator {
    return LenientHubPlanValidator(
        mutableListOf<LenientHubPlanRule>(
            LenientHubPlanRequiredTitleRule("lenient hub plan needs an entry"),
            LenientHubPlanDurationMinutesRangeRule(120, 86400),
            LenientHubPlanMaximumRangeRule(5.9, 4807.5),
            LenientHubPlanCodePrefixRule("ACM"),
            LenientHubPlanAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
