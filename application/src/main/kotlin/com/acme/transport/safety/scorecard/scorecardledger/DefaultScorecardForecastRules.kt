package com.acme.transport.safety.scorecard.scorecardledger

/** A rule a default scorecard forecast must pass. Check returns its problems, or none. */
interface DefaultScorecardForecastRule {
    fun check(item: DefaultScorecardForecast): MutableList<String>
}

data class DefaultScorecardForecastRequiredNameRule(var message: String) : DefaultScorecardForecastRule {
    override fun check(item: DefaultScorecardForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardForecastPalletsRangeRule(var min: Int, var max: Int) : DefaultScorecardForecastRule {
    override fun check(item: DefaultScorecardForecast): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class DefaultScorecardForecastRequiredChecksumRule(var message: String) : DefaultScorecardForecastRule {
    override fun check(item: DefaultScorecardForecast): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardForecastChecksumLengthRule(var maxLength: Int) : DefaultScorecardForecastRule {
    override fun check(item: DefaultScorecardForecast): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultScorecardForecastNameLengthRule(var maxLength: Int) : DefaultScorecardForecastRule {
    override fun check(item: DefaultScorecardForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default scorecard forecast. */
data class DefaultScorecardForecastValidator(var rules: MutableList<DefaultScorecardForecastRule>) {
    fun validate(item: DefaultScorecardForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultScorecardForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultScorecardForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultScorecardForecastValidator(): DefaultScorecardForecastValidator {
    return DefaultScorecardForecastValidator(
        mutableListOf<DefaultScorecardForecastRule>(
            DefaultScorecardForecastRequiredNameRule("default scorecard forecast needs this field"),
            DefaultScorecardForecastPalletsRangeRule(1000, 3600),
            DefaultScorecardForecastRequiredChecksumRule("default scorecard forecast needs this field"),
            DefaultScorecardForecastChecksumLengthRule(16),
            DefaultScorecardForecastNameLengthRule(3600),
        ),
    )
}
