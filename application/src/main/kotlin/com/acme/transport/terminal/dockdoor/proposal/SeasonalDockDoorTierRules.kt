package com.acme.transport.terminal.dockdoor.proposal

/** A rule a seasonal dock door tier must pass. Check returns its problems, or none. */
interface SeasonalDockDoorTierRule {
    fun check(item: SeasonalDockDoorTier): MutableList<String>
}

data class SeasonalDockDoorTierRequiredCodeRule(var message: String) : SeasonalDockDoorTierRule {
    override fun check(item: SeasonalDockDoorTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorTierRequiredSummaryRule(var message: String) : SeasonalDockDoorTierRule {
    override fun check(item: SeasonalDockDoorTier): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorTierSummaryLengthRule(var maxLength: Int) : SeasonalDockDoorTierRule {
    override fun check(item: SeasonalDockDoorTier): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorTierBudgetRangeRule(var min: Double, var max: Double) : SeasonalDockDoorTierRule {
    override fun check(item: SeasonalDockDoorTier): MutableList<String> {
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

data class SeasonalDockDoorTierCurrencyLengthRule(var maxLength: Int) : SeasonalDockDoorTierRule {
    override fun check(item: SeasonalDockDoorTier): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDockDoorTierRetriesRangeRule(var min: Int, var max: Int) : SeasonalDockDoorTierRule {
    override fun check(item: SeasonalDockDoorTier): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal dock door tier. */
data class SeasonalDockDoorTierValidator(var rules: MutableList<SeasonalDockDoorTierRule>) {
    fun validate(item: SeasonalDockDoorTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalDockDoorTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDockDoorTier>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDockDoorTierValidator(): SeasonalDockDoorTierValidator {
    return SeasonalDockDoorTierValidator(
        mutableListOf<SeasonalDockDoorTierRule>(
            SeasonalDockDoorTierRequiredCodeRule("seasonal dock door tier needs this field"),
            SeasonalDockDoorTierRequiredSummaryRule("seasonal dock door tier needs a value"),
            SeasonalDockDoorTierSummaryLengthRule(80),
            SeasonalDockDoorTierBudgetRangeRule(4.2, 1647.3),
            SeasonalDockDoorTierCurrencyLengthRule(1000),
            SeasonalDockDoorTierRetriesRangeRule(1000, 86400),
        ),
    )
}
