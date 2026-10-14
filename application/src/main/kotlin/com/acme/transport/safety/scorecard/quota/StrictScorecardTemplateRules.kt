package com.acme.transport.safety.scorecard.quota

/** A rule a strict scorecard template must pass. Check returns its problems, or none. */
interface StrictScorecardTemplateRule {
    fun check(item: StrictScorecardTemplate): MutableList<String>
}

data class StrictScorecardTemplateCodeLengthRule(var maxLength: Int) : StrictScorecardTemplateRule {
    override fun check(item: StrictScorecardTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardTemplateStartsAtRangeRule(var min: Int, var max: Int) : StrictScorecardTemplateRule {
    override fun check(item: StrictScorecardTemplate): MutableList<String> {
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

data class StrictScorecardTemplateCodePrefixRule(var prefix: String) : StrictScorecardTemplateRule {
    override fun check(item: StrictScorecardTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardTemplateRequiredTitleRule(var message: String) : StrictScorecardTemplateRule {
    override fun check(item: StrictScorecardTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardTemplateVisibleGuardRule(var limit: Int) : StrictScorecardTemplateRule {
    override fun check(item: StrictScorecardTemplate): MutableList<String> {
        if (item.visible && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class StrictScorecardTemplateDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : StrictScorecardTemplateRule {
    override fun check(item: StrictScorecardTemplate): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict scorecard template. */
data class StrictScorecardTemplateValidator(var rules: MutableList<StrictScorecardTemplateRule>) {
    fun validate(item: StrictScorecardTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictScorecardTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictScorecardTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictScorecardTemplateValidator(): StrictScorecardTemplateValidator {
    return StrictScorecardTemplateValidator(
        mutableListOf<StrictScorecardTemplateRule>(
            StrictScorecardTemplateCodeLengthRule(255),
            StrictScorecardTemplateStartsAtRangeRule(80, 1000),
            StrictScorecardTemplateCodePrefixRule("X"),
            StrictScorecardTemplateRequiredTitleRule("strict scorecard template needs an entry"),
            StrictScorecardTemplateVisibleGuardRule(120),
            StrictScorecardTemplateDistanceKmRangeRule(4.3, 1226.9),
        ),
    )
}
