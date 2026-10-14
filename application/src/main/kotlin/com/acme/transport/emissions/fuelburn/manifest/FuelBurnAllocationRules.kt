package com.acme.transport.emissions.fuelburn.manifest

/** A rule a fuel burn allocation must pass. Check returns its problems, or none. */
interface FuelBurnAllocationRule {
    fun check(item: FuelBurnAllocation): MutableList<String>
}

data class FuelBurnAllocationRequiredCodeRule(var message: String) : FuelBurnAllocationRule {
    override fun check(item: FuelBurnAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAllocationWindowDaysRangeRule(var min: Int, var max: Int) : FuelBurnAllocationRule {
    override fun check(item: FuelBurnAllocation): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAllocationCodePrefixRule(var prefix: String) : FuelBurnAllocationRule {
    override fun check(item: FuelBurnAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FuelBurnAllocationRetriesRangeRule(var min: Int, var max: Int) : FuelBurnAllocationRule {
    override fun check(item: FuelBurnAllocation): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fuel burn allocation. */
data class FuelBurnAllocationValidator(var rules: MutableList<FuelBurnAllocationRule>) {
    fun validate(item: FuelBurnAllocation): MutableList<String> {
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

    fun requireValid(item: FuelBurnAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FuelBurnAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultFuelBurnAllocationValidator(): FuelBurnAllocationValidator {
    return FuelBurnAllocationValidator(
        mutableListOf<FuelBurnAllocationRule>(
            FuelBurnAllocationRequiredCodeRule("fuel burn allocation needs a value"),
            FuelBurnAllocationWindowDaysRangeRule(80, 3600),
            FuelBurnAllocationCodePrefixRule("Q"),
            FuelBurnAllocationRetriesRangeRule(80, 120),
        ),
    )
}
