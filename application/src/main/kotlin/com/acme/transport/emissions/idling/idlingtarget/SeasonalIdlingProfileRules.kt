package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.text.normalize

/** A rule a seasonal idling profile must pass. Check returns its problems, or none. */
interface SeasonalIdlingProfileRule {
    fun check(item: SeasonalIdlingProfile): MutableList<String>
}

data class SeasonalIdlingProfileRequiredReviewerRule(var message: String) : SeasonalIdlingProfileRule {
    override fun check(item: SeasonalIdlingProfile): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingProfileApprovedGuardRule(var limit: Double) : SeasonalIdlingProfileRule {
    override fun check(item: SeasonalIdlingProfile): MutableList<String> {
        if (item.approved && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingProfileCodePrefixRule(var prefix: String) : SeasonalIdlingProfileRule {
    override fun check(item: SeasonalIdlingProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingProfileRequiredLabelRule(var message: String) : SeasonalIdlingProfileRule {
    override fun check(item: SeasonalIdlingProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingProfileBudgetRangeRule(var min: Double, var max: Double) : SeasonalIdlingProfileRule {
    override fun check(item: SeasonalIdlingProfile): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalIdlingProfileAllowedCodeRule(var allowed: MutableList<String>) : SeasonalIdlingProfileRule {
    override fun check(item: SeasonalIdlingProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal idling profile. */
data class SeasonalIdlingProfileValidator(var rules: MutableList<SeasonalIdlingProfileRule>) {
    fun validate(item: SeasonalIdlingProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalIdlingProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalIdlingProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalIdlingProfileValidator(): SeasonalIdlingProfileValidator {
    return SeasonalIdlingProfileValidator(
        mutableListOf<SeasonalIdlingProfileRule>(
            SeasonalIdlingProfileRequiredReviewerRule("seasonal idling profile needs a value"),
            SeasonalIdlingProfileApprovedGuardRule(6.5),
            SeasonalIdlingProfileCodePrefixRule("ACM"),
            SeasonalIdlingProfileRequiredLabelRule("seasonal idling profile needs this field"),
            SeasonalIdlingProfileBudgetRangeRule(7.8, 4483.6),
            SeasonalIdlingProfileAllowedCodeRule(mutableListOf<String>("pending", "legacy", "default")),
        ),
    )
}
