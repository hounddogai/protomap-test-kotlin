package com.acme.transport.emissions.fuelburn.group

/** A rule a fuel burn group must pass. Check returns its problems, or none. */
interface FuelBurnGroupRule {
    fun check(item: FuelBurnGroup): MutableList<String>
}

data class FuelBurnGroupBudgetRangeRule(var min: Double, var max: Double) : FuelBurnGroupRule {
    override fun check(item: FuelBurnGroup): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnGroupRequiredVehicleIdRule(var message: String) : FuelBurnGroupRule {
    override fun check(item: FuelBurnGroup): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnGroupLaneLengthRule(var maxLength: Int) : FuelBurnGroupRule {
    override fun check(item: FuelBurnGroup): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fuel burn group. */
data class FuelBurnGroupValidator(var rules: MutableList<FuelBurnGroupRule>) {
    fun validate(item: FuelBurnGroup): MutableList<String> {
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

    fun requireValid(item: FuelBurnGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnGroupValidator(): FuelBurnGroupValidator {
    return FuelBurnGroupValidator(
        mutableListOf<FuelBurnGroupRule>(
            FuelBurnGroupBudgetRangeRule(4.4, 257.3),
            FuelBurnGroupRequiredVehicleIdRule("fuel burn group needs this field"),
            FuelBurnGroupLaneLengthRule(86400),
        ),
    )
}
