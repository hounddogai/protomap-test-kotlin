package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

/** A rule a fallback backhaul metric must pass. Check returns its problems, or none. */
interface FallbackBackhaulMetricRule {
    fun check(item: FallbackBackhaulMetric): MutableList<String>
}

data class FallbackBackhaulMetricCarrierLengthRule(var maxLength: Int) : FallbackBackhaulMetricRule {
    override fun check(item: FallbackBackhaulMetric): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulMetricRequiredCodeRule(var message: String) : FallbackBackhaulMetricRule {
    override fun check(item: FallbackBackhaulMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulMetricLabelLengthRule(var maxLength: Int) : FallbackBackhaulMetricRule {
    override fun check(item: FallbackBackhaulMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback backhaul metric. */
data class FallbackBackhaulMetricValidator(var rules: MutableList<FallbackBackhaulMetricRule>) {
    fun validate(item: FallbackBackhaulMetric): MutableList<String> {
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

    fun requireValid(item: FallbackBackhaulMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulMetricValidator(): FallbackBackhaulMetricValidator {
    return FallbackBackhaulMetricValidator(
        mutableListOf<FallbackBackhaulMetricRule>(
            FallbackBackhaulMetricCarrierLengthRule(1000),
            FallbackBackhaulMetricRequiredCodeRule("fallback backhaul metric needs this field"),
            FallbackBackhaulMetricLabelLengthRule(120),
        ),
    )
}
