package com.acme.transport.emissions.fuelburn.window

/** A rule a rolling fuel burn allocation must pass. Check returns its problems, or none. */
interface RollingFuelBurnAllocationRule {
    fun check(item: RollingFuelBurnAllocation): MutableList<String>
}

data class RollingFuelBurnAllocationVersionRangeRule(
    var min: Int,
    var max: Int,
) : RollingFuelBurnAllocationRule {
    override fun check(item: RollingFuelBurnAllocation): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnAllocationRequiredTrailerIdRule(var message: String) : RollingFuelBurnAllocationRule {
    override fun check(item: RollingFuelBurnAllocation): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnAllocationCodePrefixRule(var prefix: String) : RollingFuelBurnAllocationRule {
    override fun check(item: RollingFuelBurnAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling fuel burn allocation. */
data class RollingFuelBurnAllocationValidator(var rules: MutableList<RollingFuelBurnAllocationRule>) {
    fun validate(item: RollingFuelBurnAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnAllocationValidator(): RollingFuelBurnAllocationValidator {
    return RollingFuelBurnAllocationValidator(
        mutableListOf<RollingFuelBurnAllocationRule>(
            RollingFuelBurnAllocationVersionRangeRule(16, 86400),
            RollingFuelBurnAllocationRequiredTrailerIdRule("rolling fuel burn allocation needs an entry"),
            RollingFuelBurnAllocationCodePrefixRule("X"),
        ),
    )
}
