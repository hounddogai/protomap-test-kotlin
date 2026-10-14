package com.acme.transport.emissions.fuelburn.allocation

/** A rule a regional fuel burn allocation must pass. Check returns its problems, or none. */
interface RegionalFuelBurnAllocationRule {
    fun check(item: RegionalFuelBurnAllocation): MutableList<String>
}

data class RegionalFuelBurnAllocationAmountRangeRule(
    var min: Double,
    var max: Double,
) : RegionalFuelBurnAllocationRule {
    override fun check(item: RegionalFuelBurnAllocation): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnAllocationRequiredCategoryRule(
    var message: String,
) : RegionalFuelBurnAllocationRule {
    override fun check(item: RegionalFuelBurnAllocation): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnAllocationActiveGuardRule(var limit: Double) : RegionalFuelBurnAllocationRule {
    override fun check(item: RegionalFuelBurnAllocation): MutableList<String> {
        if (item.active && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnAllocationCarrierLengthRule(var maxLength: Int) : RegionalFuelBurnAllocationRule {
    override fun check(item: RegionalFuelBurnAllocation): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalFuelBurnAllocationCodePrefixRule(var prefix: String) : RegionalFuelBurnAllocationRule {
    override fun check(item: RegionalFuelBurnAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional fuel burn allocation. */
data class RegionalFuelBurnAllocationValidator(var rules: MutableList<RegionalFuelBurnAllocationRule>) {
    fun validate(item: RegionalFuelBurnAllocation): MutableList<String> {
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

    fun requireValid(item: RegionalFuelBurnAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalFuelBurnAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalFuelBurnAllocationValidator(): RegionalFuelBurnAllocationValidator {
    return RegionalFuelBurnAllocationValidator(
        mutableListOf<RegionalFuelBurnAllocationRule>(
            RegionalFuelBurnAllocationAmountRangeRule(2.3, 4911.4),
            RegionalFuelBurnAllocationRequiredCategoryRule("regional fuel burn allocation needs a value"),
            RegionalFuelBurnAllocationActiveGuardRule(3.1),
            RegionalFuelBurnAllocationCarrierLengthRule(255),
            RegionalFuelBurnAllocationCodePrefixRule("OPS"),
        ),
    )
}
