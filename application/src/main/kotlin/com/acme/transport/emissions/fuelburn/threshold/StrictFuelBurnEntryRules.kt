package com.acme.transport.emissions.fuelburn.threshold

import com.acme.transport.kit.text.normalize

/** A rule a strict fuel burn entry must pass. Check returns its problems, or none. */
interface StrictFuelBurnEntryRule {
    fun check(item: StrictFuelBurnEntry): MutableList<String>
}

data class StrictFuelBurnEntryCostRangeRule(var min: Double, var max: Double) : StrictFuelBurnEntryRule {
    override fun check(item: StrictFuelBurnEntry): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelBurnEntryRequiredLabelRule(var message: String) : StrictFuelBurnEntryRule {
    override fun check(item: StrictFuelBurnEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictFuelBurnEntryWeightRangeRule(var min: Double, var max: Double) : StrictFuelBurnEntryRule {
    override fun check(item: StrictFuelBurnEntry): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class StrictFuelBurnEntryAllowedLabelRule(var allowed: MutableList<String>) : StrictFuelBurnEntryRule {
    override fun check(item: StrictFuelBurnEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict fuel burn entry. */
data class StrictFuelBurnEntryValidator(var rules: MutableList<StrictFuelBurnEntryRule>) {
    fun validate(item: StrictFuelBurnEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictFuelBurnEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictFuelBurnEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictFuelBurnEntryValidator(): StrictFuelBurnEntryValidator {
    return StrictFuelBurnEntryValidator(
        mutableListOf<StrictFuelBurnEntryRule>(
            StrictFuelBurnEntryCostRangeRule(8.2, 3271.1),
            StrictFuelBurnEntryRequiredLabelRule("strict fuel burn entry needs a value"),
            StrictFuelBurnEntryWeightRangeRule(6.7, 4920.7),
            StrictFuelBurnEntryAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
