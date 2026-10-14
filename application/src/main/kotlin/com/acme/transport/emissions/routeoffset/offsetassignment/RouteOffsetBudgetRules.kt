package com.acme.transport.emissions.routeoffset.offsetassignment

/** A rule a route offset budget must pass. Check returns its problems, or none. */
interface RouteOffsetBudgetRule {
    fun check(item: RouteOffsetBudget): MutableList<String>
}

data class RouteOffsetBudgetRequiredDepotRule(var message: String) : RouteOffsetBudgetRule {
    override fun check(item: RouteOffsetBudget): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetBudgetFloorValueRangeRule(var min: Double, var max: Double) : RouteOffsetBudgetRule {
    override fun check(item: RouteOffsetBudget): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetBudgetSequenceRangeRule(var min: Int, var max: Int) : RouteOffsetBudgetRule {
    override fun check(item: RouteOffsetBudget): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetBudgetCodePrefixRule(var prefix: String) : RouteOffsetBudgetRule {
    override fun check(item: RouteOffsetBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetBudgetRequiredTimezoneRule(var message: String) : RouteOffsetBudgetRule {
    override fun check(item: RouteOffsetBudget): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset budget. */
data class RouteOffsetBudgetValidator(var rules: MutableList<RouteOffsetBudgetRule>) {
    fun validate(item: RouteOffsetBudget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetBudgetValidator(): RouteOffsetBudgetValidator {
    return RouteOffsetBudgetValidator(
        mutableListOf<RouteOffsetBudgetRule>(
            RouteOffsetBudgetRequiredDepotRule("route offset budget needs this field"),
            RouteOffsetBudgetFloorValueRangeRule(1.1, 1220.0),
            RouteOffsetBudgetSequenceRangeRule(120, 1000),
            RouteOffsetBudgetCodePrefixRule("OPS"),
            RouteOffsetBudgetRequiredTimezoneRule("route offset budget needs a value"),
        ),
    )
}
