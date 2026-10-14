package com.acme.transport.network.transittime.allocation

/** A rule a transit time allocation must pass. Check returns its problems, or none. */
interface TransitTimeAllocationRule {
    fun check(item: TransitTimeAllocation): MutableList<String>
}

data class TransitTimeAllocationNameLengthRule(var maxLength: Int) : TransitTimeAllocationRule {
    override fun check(item: TransitTimeAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAllocationCostRangeRule(var min: Double, var max: Double) : TransitTimeAllocationRule {
    override fun check(item: TransitTimeAllocation): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAllocationRequiredTimezoneRule(var message: String) : TransitTimeAllocationRule {
    override fun check(item: TransitTimeAllocation): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAllocationRequiredVehicleIdRule(var message: String) : TransitTimeAllocationRule {
    override fun check(item: TransitTimeAllocation): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAllocationCodeLengthRule(var maxLength: Int) : TransitTimeAllocationRule {
    override fun check(item: TransitTimeAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeAllocationTimezoneLengthRule(var maxLength: Int) : TransitTimeAllocationRule {
    override fun check(item: TransitTimeAllocation): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time allocation. */
data class TransitTimeAllocationValidator(var rules: MutableList<TransitTimeAllocationRule>) {
    fun validate(item: TransitTimeAllocation): MutableList<String> {
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

    fun requireValid(item: TransitTimeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeAllocationValidator(): TransitTimeAllocationValidator {
    return TransitTimeAllocationValidator(
        mutableListOf<TransitTimeAllocationRule>(
            TransitTimeAllocationNameLengthRule(120),
            TransitTimeAllocationCostRangeRule(7.7, 4755.5),
            TransitTimeAllocationRequiredTimezoneRule("transit time allocation needs this field"),
            TransitTimeAllocationRequiredVehicleIdRule("transit time allocation needs a value"),
            TransitTimeAllocationCodeLengthRule(64),
            TransitTimeAllocationTimezoneLengthRule(16),
        ),
    )
}
