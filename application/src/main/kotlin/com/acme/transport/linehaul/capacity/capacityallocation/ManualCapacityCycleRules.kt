package com.acme.transport.linehaul.capacity.capacityallocation

/** A rule a manual capacity cycle must pass. Check returns its problems, or none. */
interface ManualCapacityCycleRule {
    fun check(item: ManualCapacityCycle): MutableList<String>
}

data class ManualCapacityCycleRequiredDriverIdRule(var message: String) : ManualCapacityCycleRule {
    override fun check(item: ManualCapacityCycle): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCapacityCycleCodePrefixRule(var prefix: String) : ManualCapacityCycleRule {
    override fun check(item: ManualCapacityCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualCapacityCycleCostRangeRule(var min: Double, var max: Double) : ManualCapacityCycleRule {
    override fun check(item: ManualCapacityCycle): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual capacity cycle. */
data class ManualCapacityCycleValidator(var rules: MutableList<ManualCapacityCycleRule>) {
    fun validate(item: ManualCapacityCycle): MutableList<String> {
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

    fun requireValid(item: ManualCapacityCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCapacityCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCapacityCycleValidator(): ManualCapacityCycleValidator {
    return ManualCapacityCycleValidator(
        mutableListOf<ManualCapacityCycleRule>(
            ManualCapacityCycleRequiredDriverIdRule("manual capacity cycle needs an entry"),
            ManualCapacityCycleCodePrefixRule("REF"),
            ManualCapacityCycleCostRangeRule(9.3, 2467.1),
        ),
    )
}
