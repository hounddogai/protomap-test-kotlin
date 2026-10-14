package com.acme.transport.emissions.emissionfactor.entry

/** A rule a adaptive emission factor entry must pass. Check returns its problems, or none. */
interface AdaptiveEmissionFactorEntryRule {
    fun check(item: AdaptiveEmissionFactorEntry): MutableList<String>
}

data class AdaptiveEmissionFactorEntryFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveEmissionFactorEntryRule {
    override fun check(item: AdaptiveEmissionFactorEntry): MutableList<String> {
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

data class AdaptiveEmissionFactorEntryCostRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveEmissionFactorEntryRule {
    override fun check(item: AdaptiveEmissionFactorEntry): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveEmissionFactorEntryRequiredTitleRule(var message: String) : AdaptiveEmissionFactorEntryRule {
    override fun check(item: AdaptiveEmissionFactorEntry): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive emission factor entry. */
data class AdaptiveEmissionFactorEntryValidator(var rules: MutableList<AdaptiveEmissionFactorEntryRule>) {
    fun validate(item: AdaptiveEmissionFactorEntry): MutableList<String> {
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

    fun requireValid(item: AdaptiveEmissionFactorEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveEmissionFactorEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveEmissionFactorEntryValidator(): AdaptiveEmissionFactorEntryValidator {
    return AdaptiveEmissionFactorEntryValidator(
        mutableListOf<AdaptiveEmissionFactorEntryRule>(
            AdaptiveEmissionFactorEntryFuelLitersRangeRule(1.8, 3874.3),
            AdaptiveEmissionFactorEntryCostRangeRule(3.6, 4844.6),
            AdaptiveEmissionFactorEntryRequiredTitleRule("adaptive emission factor entry needs this field"),
        ),
    )
}
