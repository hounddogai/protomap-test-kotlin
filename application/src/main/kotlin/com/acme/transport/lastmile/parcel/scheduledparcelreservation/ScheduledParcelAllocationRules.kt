package com.acme.transport.lastmile.parcel.scheduledparcelreservation

/** A rule a scheduled parcel allocation must pass. Check returns its problems, or none. */
interface ScheduledParcelAllocationRule {
    fun check(item: ScheduledParcelAllocation): MutableList<String>
}

data class ScheduledParcelAllocationFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledParcelAllocationRule {
    override fun check(item: ScheduledParcelAllocation): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelAllocationCodePrefixRule(var prefix: String) : ScheduledParcelAllocationRule {
    override fun check(item: ScheduledParcelAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledParcelAllocationRequiredRouteCodeRule(var message: String) : ScheduledParcelAllocationRule {
    override fun check(item: ScheduledParcelAllocation): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled parcel allocation. */
data class ScheduledParcelAllocationValidator(var rules: MutableList<ScheduledParcelAllocationRule>) {
    fun validate(item: ScheduledParcelAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledParcelAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledParcelAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledParcelAllocationValidator(): ScheduledParcelAllocationValidator {
    return ScheduledParcelAllocationValidator(
        mutableListOf<ScheduledParcelAllocationRule>(
            ScheduledParcelAllocationFuelLitersRangeRule(0.6, 2161.3),
            ScheduledParcelAllocationCodePrefixRule("Q"),
            ScheduledParcelAllocationRequiredRouteCodeRule("scheduled parcel allocation needs a value"),
        ),
    )
}
