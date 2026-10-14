package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.text.normalize

/** A rule a adaptive node metric must pass. Check returns its problems, or none. */
interface AdaptiveNodeMetricRule {
    fun check(item: AdaptiveNodeMetric): MutableList<String>
}

data class AdaptiveNodeMetricCodeLengthRule(var maxLength: Int) : AdaptiveNodeMetricRule {
    override fun check(item: AdaptiveNodeMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeMetricAllowedTitleRule(var allowed: MutableList<String>) : AdaptiveNodeMetricRule {
    override fun check(item: AdaptiveNodeMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeMetricRateRangeRule(var min: Double, var max: Double) : AdaptiveNodeMetricRule {
    override fun check(item: AdaptiveNodeMetric): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeMetricRequiredCodeRule(var message: String) : AdaptiveNodeMetricRule {
    override fun check(item: AdaptiveNodeMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive node metric. */
data class AdaptiveNodeMetricValidator(var rules: MutableList<AdaptiveNodeMetricRule>) {
    fun validate(item: AdaptiveNodeMetric): MutableList<String> {
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

    fun requireValid(item: AdaptiveNodeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNodeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNodeMetricValidator(): AdaptiveNodeMetricValidator {
    return AdaptiveNodeMetricValidator(
        mutableListOf<AdaptiveNodeMetricRule>(
            AdaptiveNodeMetricCodeLengthRule(3600),
            AdaptiveNodeMetricAllowedTitleRule(mutableListOf<String>("fallback")),
            AdaptiveNodeMetricRateRangeRule(3.9, 3818.2),
            AdaptiveNodeMetricRequiredCodeRule("adaptive node metric needs an entry"),
        ),
    )
}
