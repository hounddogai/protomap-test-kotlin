package com.acme.transport.terminal.traileryard.yardforecast

/** A rule a seasonal trailer yard score must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardScoreRule {
    fun check(item: SeasonalTrailerYardScore): MutableList<String>
}

data class SeasonalTrailerYardScoreRequiredCodeRule(var message: String) : SeasonalTrailerYardScoreRule {
    override fun check(item: SeasonalTrailerYardScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScoreTitleLengthRule(var maxLength: Int) : SeasonalTrailerYardScoreRule {
    override fun check(item: SeasonalTrailerYardScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScoreRequiredCategoryRule(var message: String) : SeasonalTrailerYardScoreRule {
    override fun check(item: SeasonalTrailerYardScore): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardScoreCostRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTrailerYardScoreRule {
    override fun check(item: SeasonalTrailerYardScore): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal trailer yard score. */
data class SeasonalTrailerYardScoreValidator(var rules: MutableList<SeasonalTrailerYardScoreRule>) {
    fun validate(item: SeasonalTrailerYardScore): MutableList<String> {
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

    fun requireValid(item: SeasonalTrailerYardScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardScore>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardScoreValidator(): SeasonalTrailerYardScoreValidator {
    return SeasonalTrailerYardScoreValidator(
        mutableListOf<SeasonalTrailerYardScoreRule>(
            SeasonalTrailerYardScoreRequiredCodeRule("seasonal trailer yard score needs an entry"),
            SeasonalTrailerYardScoreTitleLengthRule(64),
            SeasonalTrailerYardScoreRequiredCategoryRule("seasonal trailer yard score needs an entry"),
            SeasonalTrailerYardScoreCostRangeRule(6.9, 4853.9),
        ),
    )
}
