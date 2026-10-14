package com.acme.transport.terminal.traileryard.plan

/** A rule a tiered trailer yard metric must pass. Check returns its problems, or none. */
interface TieredTrailerYardMetricRule {
    fun check(item: TieredTrailerYardMetric): MutableList<String>
}

data class TieredTrailerYardMetricRequiredCarrierRule(var message: String) : TieredTrailerYardMetricRule {
    override fun check(item: TieredTrailerYardMetric): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardMetricRouteCodeLengthRule(var maxLength: Int) : TieredTrailerYardMetricRule {
    override fun check(item: TieredTrailerYardMetric): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardMetricRetriesRangeRule(var min: Int, var max: Int) : TieredTrailerYardMetricRule {
    override fun check(item: TieredTrailerYardMetric): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class TieredTrailerYardMetricLabelLengthRule(var maxLength: Int) : TieredTrailerYardMetricRule {
    override fun check(item: TieredTrailerYardMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardMetricCodePrefixRule(var prefix: String) : TieredTrailerYardMetricRule {
    override fun check(item: TieredTrailerYardMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredTrailerYardMetricRequiredCodeRule(var message: String) : TieredTrailerYardMetricRule {
    override fun check(item: TieredTrailerYardMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered trailer yard metric. */
data class TieredTrailerYardMetricValidator(var rules: MutableList<TieredTrailerYardMetricRule>) {
    fun validate(item: TieredTrailerYardMetric): MutableList<String> {
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

    fun requireValid(item: TieredTrailerYardMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredTrailerYardMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredTrailerYardMetricValidator(): TieredTrailerYardMetricValidator {
    return TieredTrailerYardMetricValidator(
        mutableListOf<TieredTrailerYardMetricRule>(
            TieredTrailerYardMetricRequiredCarrierRule("tiered trailer yard metric needs this field"),
            TieredTrailerYardMetricRouteCodeLengthRule(1000),
            TieredTrailerYardMetricRetriesRangeRule(64, 120),
            TieredTrailerYardMetricLabelLengthRule(16),
            TieredTrailerYardMetricCodePrefixRule("ACM"),
            TieredTrailerYardMetricRequiredCodeRule("tiered trailer yard metric needs this field"),
        ),
    )
}
