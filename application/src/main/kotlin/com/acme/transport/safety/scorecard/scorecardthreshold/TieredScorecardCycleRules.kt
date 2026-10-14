package com.acme.transport.safety.scorecard.scorecardthreshold

/** A rule a tiered scorecard cycle must pass. Check returns its problems, or none. */
interface TieredScorecardCycleRule {
    fun check(item: TieredScorecardCycle): MutableList<String>
}

data class TieredScorecardCycleTitleLengthRule(var maxLength: Int) : TieredScorecardCycleRule {
    override fun check(item: TieredScorecardCycle): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardCycleCodePrefixRule(var prefix: String) : TieredScorecardCycleRule {
    override fun check(item: TieredScorecardCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardCycleEndsAtRangeRule(var min: Int, var max: Int) : TieredScorecardCycleRule {
    override fun check(item: TieredScorecardCycle): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class TieredScorecardCycleRequiredCodeRule(var message: String) : TieredScorecardCycleRule {
    override fun check(item: TieredScorecardCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardCycleOdometerRangeRule(var min: Int, var max: Int) : TieredScorecardCycleRule {
    override fun check(item: TieredScorecardCycle): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredScorecardCycleRateRangeRule(var min: Double, var max: Double) : TieredScorecardCycleRule {
    override fun check(item: TieredScorecardCycle): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered scorecard cycle. */
data class TieredScorecardCycleValidator(var rules: MutableList<TieredScorecardCycleRule>) {
    fun validate(item: TieredScorecardCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredScorecardCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredScorecardCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredScorecardCycleValidator(): TieredScorecardCycleValidator {
    return TieredScorecardCycleValidator(
        mutableListOf<TieredScorecardCycleRule>(
            TieredScorecardCycleTitleLengthRule(64),
            TieredScorecardCycleCodePrefixRule("X"),
            TieredScorecardCycleEndsAtRangeRule(120, 86400),
            TieredScorecardCycleRequiredCodeRule("tiered scorecard cycle needs this field"),
            TieredScorecardCycleOdometerRangeRule(1000, 86400),
            TieredScorecardCycleRateRangeRule(2.7, 4278.1),
        ),
    )
}
