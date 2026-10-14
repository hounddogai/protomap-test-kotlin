package com.acme.transport.emissions.fuelburn.burnvariance

/** A rule a scheduled fuel burn allocation must pass. Check returns its problems, or none. */
interface ScheduledFuelBurnAllocationRule {
    fun check(item: ScheduledFuelBurnAllocation): MutableList<String>
}

data class ScheduledFuelBurnAllocationWeightRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledFuelBurnAllocationRule {
    override fun check(item: ScheduledFuelBurnAllocation): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnAllocationCodePrefixRule(var prefix: String) : ScheduledFuelBurnAllocationRule {
    override fun check(item: ScheduledFuelBurnAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnAllocationLabelLengthRule(var maxLength: Int) : ScheduledFuelBurnAllocationRule {
    override fun check(item: ScheduledFuelBurnAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel burn allocation. */
data class ScheduledFuelBurnAllocationValidator(var rules: MutableList<ScheduledFuelBurnAllocationRule>) {
    fun validate(item: ScheduledFuelBurnAllocation): MutableList<String> {
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

    fun requireValid(item: ScheduledFuelBurnAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelBurnAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelBurnAllocationValidator(): ScheduledFuelBurnAllocationValidator {
    return ScheduledFuelBurnAllocationValidator(
        mutableListOf<ScheduledFuelBurnAllocationRule>(
            ScheduledFuelBurnAllocationWeightRangeRule(6.2, 894.2),
            ScheduledFuelBurnAllocationCodePrefixRule("ACM"),
            ScheduledFuelBurnAllocationLabelLengthRule(86400),
        ),
    )
}
