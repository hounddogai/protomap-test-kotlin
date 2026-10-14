package com.acme.transport.network.node.metric

/** A rule a fallback node metric must pass. Check returns its problems, or none. */
interface FallbackNodeMetricRule {
    fun check(item: FallbackNodeMetric): MutableList<String>
}

data class FallbackNodeMetricActiveGuardRule(var limit: Double) : FallbackNodeMetricRule {
    override fun check(item: FallbackNodeMetric): MutableList<String> {
        if (item.active && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeMetricRequiredTitleRule(var message: String) : FallbackNodeMetricRule {
    override fun check(item: FallbackNodeMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackNodeMetricTitleLengthRule(var maxLength: Int) : FallbackNodeMetricRule {
    override fun check(item: FallbackNodeMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback node metric. */
data class FallbackNodeMetricValidator(var rules: MutableList<FallbackNodeMetricRule>) {
    fun validate(item: FallbackNodeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackNodeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackNodeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackNodeMetricValidator(): FallbackNodeMetricValidator {
    return FallbackNodeMetricValidator(
        mutableListOf<FallbackNodeMetricRule>(
            FallbackNodeMetricActiveGuardRule(5.4),
            FallbackNodeMetricRequiredTitleRule("fallback node metric needs a value"),
            FallbackNodeMetricTitleLengthRule(80),
        ),
    )
}
