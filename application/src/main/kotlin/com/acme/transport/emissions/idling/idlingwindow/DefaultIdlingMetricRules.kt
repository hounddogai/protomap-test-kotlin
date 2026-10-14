package com.acme.transport.emissions.idling.idlingwindow

/** A rule a default idling metric must pass. Check returns its problems, or none. */
interface DefaultIdlingMetricRule {
    fun check(item: DefaultIdlingMetric): MutableList<String>
}

data class DefaultIdlingMetricDistanceKmRangeRule(var min: Double, var max: Double) : DefaultIdlingMetricRule {
    override fun check(item: DefaultIdlingMetric): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingMetricCodePrefixRule(var prefix: String) : DefaultIdlingMetricRule {
    override fun check(item: DefaultIdlingMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingMetricTitleLengthRule(var maxLength: Int) : DefaultIdlingMetricRule {
    override fun check(item: DefaultIdlingMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingMetricLimitRangeRule(var min: Int, var max: Int) : DefaultIdlingMetricRule {
    override fun check(item: DefaultIdlingMetric): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default idling metric. */
data class DefaultIdlingMetricValidator(var rules: MutableList<DefaultIdlingMetricRule>) {
    fun validate(item: DefaultIdlingMetric): MutableList<String> {
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

    fun requireValid(item: DefaultIdlingMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultIdlingMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultIdlingMetricValidator(): DefaultIdlingMetricValidator {
    return DefaultIdlingMetricValidator(
        mutableListOf<DefaultIdlingMetricRule>(
            DefaultIdlingMetricDistanceKmRangeRule(7.6, 2320.4),
            DefaultIdlingMetricCodePrefixRule("REF"),
            DefaultIdlingMetricTitleLengthRule(80),
            DefaultIdlingMetricLimitRangeRule(255, 3600),
        ),
    )
}
