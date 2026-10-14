package com.acme.transport.fleetops.tire.segment

/** A rule a seasonal tire budget must pass. Check returns its problems, or none. */
interface SeasonalTireBudgetRule {
    fun check(item: SeasonalTireBudget): MutableList<String>
}

data class SeasonalTireBudgetEtaMinutesRangeRule(var min: Int, var max: Int) : SeasonalTireBudgetRule {
    override fun check(item: SeasonalTireBudget): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class SeasonalTireBudgetRequiredNameRule(var message: String) : SeasonalTireBudgetRule {
    override fun check(item: SeasonalTireBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTireBudgetTrailerIdLengthRule(var maxLength: Int) : SeasonalTireBudgetRule {
    override fun check(item: SeasonalTireBudget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal tire budget. */
data class SeasonalTireBudgetValidator(var rules: MutableList<SeasonalTireBudgetRule>) {
    fun validate(item: SeasonalTireBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTireBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTireBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTireBudgetValidator(): SeasonalTireBudgetValidator {
    return SeasonalTireBudgetValidator(
        mutableListOf<SeasonalTireBudgetRule>(
            SeasonalTireBudgetEtaMinutesRangeRule(16, 80),
            SeasonalTireBudgetRequiredNameRule("seasonal tire budget needs an entry"),
            SeasonalTireBudgetTrailerIdLengthRule(80),
        ),
    )
}
