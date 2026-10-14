package com.acme.transport.linehaul.trailer.summary

/** A rule a seasonal trailer summary must pass. Check returns its problems, or none. */
interface SeasonalTrailerSummaryRule {
    fun check(item: SeasonalTrailerSummary): MutableList<String>
}

data class SeasonalTrailerSummaryExpiresAtRangeRule(var min: Int, var max: Int) : SeasonalTrailerSummaryRule {
    override fun check(item: SeasonalTrailerSummary): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerSummaryRequiredTitleRule(var message: String) : SeasonalTrailerSummaryRule {
    override fun check(item: SeasonalTrailerSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerSummaryRequiredVehicleIdRule(var message: String) : SeasonalTrailerSummaryRule {
    override fun check(item: SeasonalTrailerSummary): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer summary. */
data class SeasonalTrailerSummaryValidator(var rules: MutableList<SeasonalTrailerSummaryRule>) {
    fun validate(item: SeasonalTrailerSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTrailerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerSummaryValidator(): SeasonalTrailerSummaryValidator {
    return SeasonalTrailerSummaryValidator(
        mutableListOf<SeasonalTrailerSummaryRule>(
            SeasonalTrailerSummaryExpiresAtRangeRule(64, 120),
            SeasonalTrailerSummaryRequiredTitleRule("seasonal trailer summary needs a value"),
            SeasonalTrailerSummaryRequiredVehicleIdRule("seasonal trailer summary needs a value"),
        ),
    )
}
