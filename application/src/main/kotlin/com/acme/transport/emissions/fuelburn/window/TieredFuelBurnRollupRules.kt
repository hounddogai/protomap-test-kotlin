package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.text.normalize

/** A rule a tiered fuel burn rollup must pass. Check returns its problems, or none. */
interface TieredFuelBurnRollupRule {
    fun check(item: TieredFuelBurnRollup): MutableList<String>
}

data class TieredFuelBurnRollupRequiredCodeRule(var message: String) : TieredFuelBurnRollupRule {
    override fun check(item: TieredFuelBurnRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnRollupAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : TieredFuelBurnRollupRule {
    override fun check(item: TieredFuelBurnRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnRollupLaneLengthRule(var maxLength: Int) : TieredFuelBurnRollupRule {
    override fun check(item: TieredFuelBurnRollup): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelBurnRollupPalletsRangeRule(var min: Int, var max: Int) : TieredFuelBurnRollupRule {
    override fun check(item: TieredFuelBurnRollup): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel burn rollup. */
data class TieredFuelBurnRollupValidator(var rules: MutableList<TieredFuelBurnRollupRule>) {
    fun validate(item: TieredFuelBurnRollup): MutableList<String> {
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

    fun requireValid(item: TieredFuelBurnRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelBurnRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelBurnRollupValidator(): TieredFuelBurnRollupValidator {
    return TieredFuelBurnRollupValidator(
        mutableListOf<TieredFuelBurnRollupRule>(
            TieredFuelBurnRollupRequiredCodeRule("tiered fuel burn rollup needs a value"),
            TieredFuelBurnRollupAllowedVehicleIdRule(mutableListOf<String>("capped", "weighted", "lenient")),
            TieredFuelBurnRollupLaneLengthRule(64),
            TieredFuelBurnRollupPalletsRangeRule(120, 3600),
        ),
    )
}
