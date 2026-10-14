package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.text.normalize

/** A rule a weighted dock door profile must pass. Check returns its problems, or none. */
interface WeightedDockDoorProfileRule {
    fun check(item: WeightedDockDoorProfile): MutableList<String>
}

data class WeightedDockDoorProfileRequiredSummaryRule(var message: String) : WeightedDockDoorProfileRule {
    override fun check(item: WeightedDockDoorProfile): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorProfileAllowedSummaryRule(
    var allowed: MutableList<String>,
) : WeightedDockDoorProfileRule {
    override fun check(item: WeightedDockDoorProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorProfileCodePrefixRule(var prefix: String) : WeightedDockDoorProfileRule {
    override fun check(item: WeightedDockDoorProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorProfileRequiredRouteCodeRule(var message: String) : WeightedDockDoorProfileRule {
    override fun check(item: WeightedDockDoorProfile): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDockDoorProfileCapacityRangeRule(var min: Int, var max: Int) : WeightedDockDoorProfileRule {
    override fun check(item: WeightedDockDoorProfile): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a weighted dock door profile. */
data class WeightedDockDoorProfileValidator(var rules: MutableList<WeightedDockDoorProfileRule>) {
    fun validate(item: WeightedDockDoorProfile): MutableList<String> {
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

    fun requireValid(item: WeightedDockDoorProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDockDoorProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDockDoorProfileValidator(): WeightedDockDoorProfileValidator {
    return WeightedDockDoorProfileValidator(
        mutableListOf<WeightedDockDoorProfileRule>(
            WeightedDockDoorProfileRequiredSummaryRule("weighted dock door profile needs an entry"),
            WeightedDockDoorProfileAllowedSummaryRule(mutableListOf<String>("tiered")),
            WeightedDockDoorProfileCodePrefixRule("Q"),
            WeightedDockDoorProfileRequiredRouteCodeRule("weighted dock door profile needs a value"),
            WeightedDockDoorProfileCapacityRangeRule(32, 255),
        ),
    )
}
