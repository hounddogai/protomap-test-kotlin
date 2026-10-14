package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize

/** A rule a tiered broker metric must pass. Check returns its problems, or none. */
interface TieredBrokerMetricRule {
    fun check(item: TieredBrokerMetric): MutableList<String>
}

data class TieredBrokerMetricRequiredCodeRule(var message: String) : TieredBrokerMetricRule {
    override fun check(item: TieredBrokerMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerMetricAllowedTitleRule(var allowed: MutableList<String>) : TieredBrokerMetricRule {
    override fun check(item: TieredBrokerMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class TieredBrokerMetricEndsAtRangeRule(var min: Int, var max: Int) : TieredBrokerMetricRule {
    override fun check(item: TieredBrokerMetric): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class TieredBrokerMetricScoreRangeRule(var min: Double, var max: Double) : TieredBrokerMetricRule {
    override fun check(item: TieredBrokerMetric): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered broker metric. */
data class TieredBrokerMetricValidator(var rules: MutableList<TieredBrokerMetricRule>) {
    fun validate(item: TieredBrokerMetric): MutableList<String> {
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

    fun requireValid(item: TieredBrokerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredBrokerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredBrokerMetricValidator(): TieredBrokerMetricValidator {
    return TieredBrokerMetricValidator(
        mutableListOf<TieredBrokerMetricRule>(
            TieredBrokerMetricRequiredCodeRule("tiered broker metric needs this field"),
            TieredBrokerMetricAllowedTitleRule(mutableListOf<String>("strict", "rolling")),
            TieredBrokerMetricEndsAtRangeRule(16, 255),
            TieredBrokerMetricScoreRangeRule(1.7, 472.8),
        ),
    )
}
