package com.acme.transport.terminal.dockdoor.mapping

/** A rule a adaptive dock door metric must pass. Check returns its problems, or none. */
interface AdaptiveDockDoorMetricRule {
    fun check(item: AdaptiveDockDoorMetric): MutableList<String>
}

data class AdaptiveDockDoorMetricCostRangeRule(var min: Double, var max: Double) : AdaptiveDockDoorMetricRule {
    override fun check(item: AdaptiveDockDoorMetric): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDockDoorMetricActiveGuardRule(var limit: Int) : AdaptiveDockDoorMetricRule {
    override fun check(item: AdaptiveDockDoorMetric): MutableList<String> {
        if (item.active && item.version > limit) {
            return mutableListOf<String>("version ${item.version} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDockDoorMetricCodePrefixRule(var prefix: String) : AdaptiveDockDoorMetricRule {
    override fun check(item: AdaptiveDockDoorMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dock door metric. */
data class AdaptiveDockDoorMetricValidator(var rules: MutableList<AdaptiveDockDoorMetricRule>) {
    fun validate(item: AdaptiveDockDoorMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveDockDoorMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDockDoorMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDockDoorMetricValidator(): AdaptiveDockDoorMetricValidator {
    return AdaptiveDockDoorMetricValidator(
        mutableListOf<AdaptiveDockDoorMetricRule>(
            AdaptiveDockDoorMetricCostRangeRule(4.1, 1115.9),
            AdaptiveDockDoorMetricActiveGuardRule(86400),
            AdaptiveDockDoorMetricCodePrefixRule("X"),
        ),
    )
}
