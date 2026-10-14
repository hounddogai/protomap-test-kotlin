package com.acme.transport.safety.scorecard.scorecardgroup

/** A rule a adaptive scorecard group must pass. Check returns its problems, or none. */
interface AdaptiveScorecardGroupRule {
    fun check(item: AdaptiveScorecardGroup): MutableList<String>
}

data class AdaptiveScorecardGroupBudgetRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveScorecardGroupRule {
    override fun check(item: AdaptiveScorecardGroup): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardGroupCodePrefixRule(var prefix: String) : AdaptiveScorecardGroupRule {
    override fun check(item: AdaptiveScorecardGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardGroupTitleLengthRule(var maxLength: Int) : AdaptiveScorecardGroupRule {
    override fun check(item: AdaptiveScorecardGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardGroupCeilingRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveScorecardGroupRule {
    override fun check(item: AdaptiveScorecardGroup): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class AdaptiveScorecardGroupRequiredCurrencyRule(var message: String) : AdaptiveScorecardGroupRule {
    override fun check(item: AdaptiveScorecardGroup): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveScorecardGroupCurrencyLengthRule(var maxLength: Int) : AdaptiveScorecardGroupRule {
    override fun check(item: AdaptiveScorecardGroup): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive scorecard group. */
data class AdaptiveScorecardGroupValidator(var rules: MutableList<AdaptiveScorecardGroupRule>) {
    fun validate(item: AdaptiveScorecardGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveScorecardGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveScorecardGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveScorecardGroupValidator(): AdaptiveScorecardGroupValidator {
    return AdaptiveScorecardGroupValidator(
        mutableListOf<AdaptiveScorecardGroupRule>(
            AdaptiveScorecardGroupBudgetRangeRule(3.3, 2552.8),
            AdaptiveScorecardGroupCodePrefixRule("X"),
            AdaptiveScorecardGroupTitleLengthRule(32),
            AdaptiveScorecardGroupCeilingRangeRule(4.2, 3858.0),
            AdaptiveScorecardGroupRequiredCurrencyRule("adaptive scorecard group needs an entry"),
            AdaptiveScorecardGroupCurrencyLengthRule(86400),
        ),
    )
}
