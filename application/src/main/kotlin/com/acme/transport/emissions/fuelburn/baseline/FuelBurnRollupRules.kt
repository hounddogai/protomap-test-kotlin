package com.acme.transport.emissions.fuelburn.baseline

/** A rule a fuel burn rollup must pass. Check returns its problems, or none. */
interface FuelBurnRollupRule {
    fun check(item: FuelBurnRollup): MutableList<String>
}

data class FuelBurnRollupRequiredCodeRule(var message: String) : FuelBurnRollupRule {
    override fun check(item: FuelBurnRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRollupWeightRangeRule(var min: Double, var max: Double) : FuelBurnRollupRule {
    override fun check(item: FuelBurnRollup): MutableList<String> {
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

data class FuelBurnRollupRequiredNameRule(var message: String) : FuelBurnRollupRule {
    override fun check(item: FuelBurnRollup): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnRollupDurationMinutesRangeRule(var min: Int, var max: Int) : FuelBurnRollupRule {
    override fun check(item: FuelBurnRollup): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn rollup. */
data class FuelBurnRollupValidator(var rules: MutableList<FuelBurnRollupRule>) {
    fun validate(item: FuelBurnRollup): MutableList<String> {
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

    fun requireValid(item: FuelBurnRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnRollupValidator(): FuelBurnRollupValidator {
    return FuelBurnRollupValidator(
        mutableListOf<FuelBurnRollupRule>(
            FuelBurnRollupRequiredCodeRule("fuel burn rollup needs an entry"),
            FuelBurnRollupWeightRangeRule(1.6, 2793.8),
            FuelBurnRollupRequiredNameRule("fuel burn rollup needs an entry"),
            FuelBurnRollupDurationMinutesRangeRule(32, 255),
        ),
    )
}
