package com.acme.transport.emissions.emissionfactor.factortarget

/** A rule a seasonal emission factor cycle must pass. Check returns its problems, or none. */
interface SeasonalEmissionFactorCycleRule {
    fun check(item: SeasonalEmissionFactorCycle): MutableList<String>
}

data class SeasonalEmissionFactorCycleFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalEmissionFactorCycleRule {
    override fun check(item: SeasonalEmissionFactorCycle): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalEmissionFactorCycleBudgetRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalEmissionFactorCycleRule {
    override fun check(item: SeasonalEmissionFactorCycle): MutableList<String> {
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

data class SeasonalEmissionFactorCycleRequiredDockRule(var message: String) : SeasonalEmissionFactorCycleRule {
    override fun check(item: SeasonalEmissionFactorCycle): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal emission factor cycle. */
data class SeasonalEmissionFactorCycleValidator(var rules: MutableList<SeasonalEmissionFactorCycleRule>) {
    fun validate(item: SeasonalEmissionFactorCycle): MutableList<String> {
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

    fun requireValid(item: SeasonalEmissionFactorCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalEmissionFactorCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalEmissionFactorCycleValidator(): SeasonalEmissionFactorCycleValidator {
    return SeasonalEmissionFactorCycleValidator(
        mutableListOf<SeasonalEmissionFactorCycleRule>(
            SeasonalEmissionFactorCycleFuelLitersRangeRule(5.5, 4415.4),
            SeasonalEmissionFactorCycleBudgetRangeRule(2.3, 2422.6),
            SeasonalEmissionFactorCycleRequiredDockRule("seasonal emission factor cycle needs an entry"),
        ),
    )
}
