package com.acme.transport.customs.tariff.tariffdigest

/** A rule a adaptive tariff metric must pass. Check returns its problems, or none. */
interface AdaptiveTariffMetricRule {
    fun check(item: AdaptiveTariffMetric): MutableList<String>
}

data class AdaptiveTariffMetricStopCountRangeRule(var min: Int, var max: Int) : AdaptiveTariffMetricRule {
    override fun check(item: AdaptiveTariffMetric): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMetricCodePrefixRule(var prefix: String) : AdaptiveTariffMetricRule {
    override fun check(item: AdaptiveTariffMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMetricRequiredRouteCodeRule(var message: String) : AdaptiveTariffMetricRule {
    override fun check(item: AdaptiveTariffMetric): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMetricRequiredCodeRule(var message: String) : AdaptiveTariffMetricRule {
    override fun check(item: AdaptiveTariffMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTariffMetricNameLengthRule(var maxLength: Int) : AdaptiveTariffMetricRule {
    override fun check(item: AdaptiveTariffMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tariff metric. */
data class AdaptiveTariffMetricValidator(var rules: MutableList<AdaptiveTariffMetricRule>) {
    fun validate(item: AdaptiveTariffMetric): MutableList<String> {
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

    fun requireValid(item: AdaptiveTariffMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTariffMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTariffMetricValidator(): AdaptiveTariffMetricValidator {
    return AdaptiveTariffMetricValidator(
        mutableListOf<AdaptiveTariffMetricRule>(
            AdaptiveTariffMetricStopCountRangeRule(255, 255),
            AdaptiveTariffMetricCodePrefixRule("REF"),
            AdaptiveTariffMetricRequiredRouteCodeRule("adaptive tariff metric needs a value"),
            AdaptiveTariffMetricRequiredCodeRule("adaptive tariff metric needs a value"),
            AdaptiveTariffMetricNameLengthRule(120),
        ),
    )
}
