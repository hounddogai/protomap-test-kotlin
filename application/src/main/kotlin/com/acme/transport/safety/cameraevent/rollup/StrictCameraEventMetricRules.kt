package com.acme.transport.safety.cameraevent.rollup

/** A rule a strict camera event metric must pass. Check returns its problems, or none. */
interface StrictCameraEventMetricRule {
    fun check(item: StrictCameraEventMetric): MutableList<String>
}

data class StrictCameraEventMetricRequiredCurrencyRule(var message: String) : StrictCameraEventMetricRule {
    override fun check(item: StrictCameraEventMetric): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictCameraEventMetricCodePrefixRule(var prefix: String) : StrictCameraEventMetricRule {
    override fun check(item: StrictCameraEventMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictCameraEventMetricCodeLengthRule(var maxLength: Int) : StrictCameraEventMetricRule {
    override fun check(item: StrictCameraEventMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict camera event metric. */
data class StrictCameraEventMetricValidator(var rules: MutableList<StrictCameraEventMetricRule>) {
    fun validate(item: StrictCameraEventMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictCameraEventMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictCameraEventMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictCameraEventMetricValidator(): StrictCameraEventMetricValidator {
    return StrictCameraEventMetricValidator(
        mutableListOf<StrictCameraEventMetricRule>(
            StrictCameraEventMetricRequiredCurrencyRule("strict camera event metric needs this field"),
            StrictCameraEventMetricCodePrefixRule("OPS"),
            StrictCameraEventMetricCodeLengthRule(1000),
        ),
    )
}
