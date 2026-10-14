package com.acme.transport.lastmile.attempt.attemptdraft

/** A rule a seasonal attempt entry must pass. Check returns its problems, or none. */
interface SeasonalAttemptEntryRule {
    fun check(item: SeasonalAttemptEntry): MutableList<String>
}

data class SeasonalAttemptEntryTitleLengthRule(var maxLength: Int) : SeasonalAttemptEntryRule {
    override fun check(item: SeasonalAttemptEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEntryFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalAttemptEntryRule {
    override fun check(item: SeasonalAttemptEntry): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class SeasonalAttemptEntryDistanceKmRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalAttemptEntryRule {
    override fun check(item: SeasonalAttemptEntry): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEntryRequiredCodeRule(var message: String) : SeasonalAttemptEntryRule {
    override fun check(item: SeasonalAttemptEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEntryCodeLengthRule(var maxLength: Int) : SeasonalAttemptEntryRule {
    override fun check(item: SeasonalAttemptEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAttemptEntryBatchSizeRangeRule(var min: Int, var max: Int) : SeasonalAttemptEntryRule {
    override fun check(item: SeasonalAttemptEntry): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal attempt entry. */
data class SeasonalAttemptEntryValidator(var rules: MutableList<SeasonalAttemptEntryRule>) {
    fun validate(item: SeasonalAttemptEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAttemptEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAttemptEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAttemptEntryValidator(): SeasonalAttemptEntryValidator {
    return SeasonalAttemptEntryValidator(
        mutableListOf<SeasonalAttemptEntryRule>(
            SeasonalAttemptEntryTitleLengthRule(80),
            SeasonalAttemptEntryFuelLitersRangeRule(4.4, 523.4),
            SeasonalAttemptEntryDistanceKmRangeRule(1.5, 1419.3),
            SeasonalAttemptEntryRequiredCodeRule("seasonal attempt entry needs an entry"),
            SeasonalAttemptEntryCodeLengthRule(120),
            SeasonalAttemptEntryBatchSizeRangeRule(16, 80),
        ),
    )
}
