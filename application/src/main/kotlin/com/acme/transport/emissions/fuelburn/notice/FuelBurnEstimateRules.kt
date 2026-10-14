package com.acme.transport.emissions.fuelburn.notice

/** A rule a fuel burn estimate must pass. Check returns its problems, or none. */
interface FuelBurnEstimateRule {
    fun check(item: FuelBurnEstimate): MutableList<String>
}

data class FuelBurnEstimateTitleLengthRule(var maxLength: Int) : FuelBurnEstimateRule {
    override fun check(item: FuelBurnEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnEstimateRequiredCurrencyRule(var message: String) : FuelBurnEstimateRule {
    override fun check(item: FuelBurnEstimate): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnEstimateRequiredVehicleIdRule(var message: String) : FuelBurnEstimateRule {
    override fun check(item: FuelBurnEstimate): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnEstimateVehicleIdLengthRule(var maxLength: Int) : FuelBurnEstimateRule {
    override fun check(item: FuelBurnEstimate): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn estimate. */
data class FuelBurnEstimateValidator(var rules: MutableList<FuelBurnEstimateRule>) {
    fun validate(item: FuelBurnEstimate): MutableList<String> {
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

    fun requireValid(item: FuelBurnEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnEstimateValidator(): FuelBurnEstimateValidator {
    return FuelBurnEstimateValidator(
        mutableListOf<FuelBurnEstimateRule>(
            FuelBurnEstimateTitleLengthRule(32),
            FuelBurnEstimateRequiredCurrencyRule("fuel burn estimate needs a value"),
            FuelBurnEstimateRequiredVehicleIdRule("fuel burn estimate needs an entry"),
            FuelBurnEstimateVehicleIdLengthRule(80),
        ),
    )
}
