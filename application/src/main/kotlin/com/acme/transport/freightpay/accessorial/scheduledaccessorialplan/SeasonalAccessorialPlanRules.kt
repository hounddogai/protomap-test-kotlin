package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.kit.text.normalize

/** A rule a seasonal accessorial plan must pass. Check returns its problems, or none. */
interface SeasonalAccessorialPlanRule {
    fun check(item: SeasonalAccessorialPlan): MutableList<String>
}

data class SeasonalAccessorialPlanRequiredCodeRule(var message: String) : SeasonalAccessorialPlanRule {
    override fun check(item: SeasonalAccessorialPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialPlanRequiredSourceRule(var message: String) : SeasonalAccessorialPlanRule {
    override fun check(item: SeasonalAccessorialPlan): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialPlanAllowedReviewerRule(
    var allowed: MutableList<String>,
) : SeasonalAccessorialPlanRule {
    override fun check(item: SeasonalAccessorialPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is too long")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialPlanMaximumRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalAccessorialPlanRule {
    override fun check(item: SeasonalAccessorialPlan): MutableList<String> {
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

/** Runs every rule of a seasonal accessorial plan. */
data class SeasonalAccessorialPlanValidator(var rules: MutableList<SeasonalAccessorialPlanRule>) {
    fun validate(item: SeasonalAccessorialPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAccessorialPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAccessorialPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAccessorialPlanValidator(): SeasonalAccessorialPlanValidator {
    return SeasonalAccessorialPlanValidator(
        mutableListOf<SeasonalAccessorialPlanRule>(
            SeasonalAccessorialPlanRequiredCodeRule("seasonal accessorial plan needs an entry"),
            SeasonalAccessorialPlanRequiredSourceRule("seasonal accessorial plan needs a value"),
            SeasonalAccessorialPlanAllowedReviewerRule(mutableListOf<String>("weighted")),
            SeasonalAccessorialPlanMaximumRangeRule(9.2, 126.6),
        ),
    )
}
