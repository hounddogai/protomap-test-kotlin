package com.acme.transport.fleetops.serviceplan.batchedplanrollup

/** A rule a fallback service plan rollup must pass. Check returns its problems, or none. */
interface FallbackServicePlanRollupRule {
    fun check(item: FallbackServicePlanRollup): MutableList<String>
}

data class FallbackServicePlanRollupRequiredLabelRule(var message: String) : FallbackServicePlanRollupRule {
    override fun check(item: FallbackServicePlanRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanRollupRequiredCodeRule(var message: String) : FallbackServicePlanRollupRule {
    override fun check(item: FallbackServicePlanRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanRollupUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : FallbackServicePlanRollupRule {
    override fun check(item: FallbackServicePlanRollup): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class FallbackServicePlanRollupCodePrefixRule(var prefix: String) : FallbackServicePlanRollupRule {
    override fun check(item: FallbackServicePlanRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanRollupWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackServicePlanRollupRule {
    override fun check(item: FallbackServicePlanRollup): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class FallbackServicePlanRollupRequiredRouteCodeRule(var message: String) : FallbackServicePlanRollupRule {
    override fun check(item: FallbackServicePlanRollup): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service plan rollup. */
data class FallbackServicePlanRollupValidator(var rules: MutableList<FallbackServicePlanRollupRule>) {
    fun validate(item: FallbackServicePlanRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackServicePlanRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServicePlanRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServicePlanRollupValidator(): FallbackServicePlanRollupValidator {
    return FallbackServicePlanRollupValidator(
        mutableListOf<FallbackServicePlanRollupRule>(
            FallbackServicePlanRollupRequiredLabelRule("fallback service plan rollup needs an entry"),
            FallbackServicePlanRollupRequiredCodeRule("fallback service plan rollup needs an entry"),
            FallbackServicePlanRollupUpdatedAtRangeRule(16, 255),
            FallbackServicePlanRollupCodePrefixRule("X"),
            FallbackServicePlanRollupWindowDaysRangeRule(120, 86400),
            FallbackServicePlanRollupRequiredRouteCodeRule("fallback service plan rollup needs a value"),
        ),
    )
}
