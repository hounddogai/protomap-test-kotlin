package com.acme.transport.emissions.routeoffset.offsetestimate

/** A rule a global route offset cycle must pass. Check returns its problems, or none. */
interface GlobalRouteOffsetCycleRule {
    fun check(item: GlobalRouteOffsetCycle): MutableList<String>
}

data class GlobalRouteOffsetCyclePalletsRangeRule(var min: Int, var max: Int) : GlobalRouteOffsetCycleRule {
    override fun check(item: GlobalRouteOffsetCycle): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalRouteOffsetCycleQuantityRangeRule(var min: Int, var max: Int) : GlobalRouteOffsetCycleRule {
    override fun check(item: GlobalRouteOffsetCycle): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class GlobalRouteOffsetCycleRequiredRegionRule(var message: String) : GlobalRouteOffsetCycleRule {
    override fun check(item: GlobalRouteOffsetCycle): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global route offset cycle. */
data class GlobalRouteOffsetCycleValidator(var rules: MutableList<GlobalRouteOffsetCycleRule>) {
    fun validate(item: GlobalRouteOffsetCycle): MutableList<String> {
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

    fun requireValid(item: GlobalRouteOffsetCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalRouteOffsetCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalRouteOffsetCycleValidator(): GlobalRouteOffsetCycleValidator {
    return GlobalRouteOffsetCycleValidator(
        mutableListOf<GlobalRouteOffsetCycleRule>(
            GlobalRouteOffsetCyclePalletsRangeRule(255, 255),
            GlobalRouteOffsetCycleQuantityRangeRule(120, 86400),
            GlobalRouteOffsetCycleRequiredRegionRule("global route offset cycle needs this field"),
        ),
    )
}
