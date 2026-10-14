package com.acme.transport.emissions.fuelburn.manifest

/** A rule a fuel burn batch must pass. Check returns its problems, or none. */
interface FuelBurnBatchRule {
    fun check(item: FuelBurnBatch): MutableList<String>
}

data class FuelBurnBatchCurrencyLengthRule(var maxLength: Int) : FuelBurnBatchRule {
    override fun check(item: FuelBurnBatch): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnBatchRequiredDepotRule(var message: String) : FuelBurnBatchRule {
    override fun check(item: FuelBurnBatch): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnBatchThresholdRangeRule(var min: Double, var max: Double) : FuelBurnBatchRule {
    override fun check(item: FuelBurnBatch): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class FuelBurnBatchCostRangeRule(var min: Double, var max: Double) : FuelBurnBatchRule {
    override fun check(item: FuelBurnBatch): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn batch. */
data class FuelBurnBatchValidator(var rules: MutableList<FuelBurnBatchRule>) {
    fun validate(item: FuelBurnBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FuelBurnBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnBatchValidator(): FuelBurnBatchValidator {
    return FuelBurnBatchValidator(
        mutableListOf<FuelBurnBatchRule>(
            FuelBurnBatchCurrencyLengthRule(255),
            FuelBurnBatchRequiredDepotRule("fuel burn batch needs a value"),
            FuelBurnBatchThresholdRangeRule(0.3, 3057.0),
            FuelBurnBatchCostRangeRule(5.3, 255.7),
        ),
    )
}
