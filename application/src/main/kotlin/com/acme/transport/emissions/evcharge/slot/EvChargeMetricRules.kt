package com.acme.transport.emissions.evcharge.slot

/** A rule a ev charge metric must pass. Check returns its problems, or none. */
interface EvChargeMetricRule {
    fun check(item: EvChargeMetric): MutableList<String>
}

data class EvChargeMetricRequiredCodeRule(var message: String) : EvChargeMetricRule {
    override fun check(item: EvChargeMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMetricRequiredLabelRule(var message: String) : EvChargeMetricRule {
    override fun check(item: EvChargeMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMetricLoadKgRangeRule(var min: Double, var max: Double) : EvChargeMetricRule {
    override fun check(item: EvChargeMetric): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMetricCodePrefixRule(var prefix: String) : EvChargeMetricRule {
    override fun check(item: EvChargeMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeMetricCodeLengthRule(var maxLength: Int) : EvChargeMetricRule {
    override fun check(item: EvChargeMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge metric. */
data class EvChargeMetricValidator(var rules: MutableList<EvChargeMetricRule>) {
    fun validate(item: EvChargeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeMetricValidator(): EvChargeMetricValidator {
    return EvChargeMetricValidator(
        mutableListOf<EvChargeMetricRule>(
            EvChargeMetricRequiredCodeRule("ev charge metric needs this field"),
            EvChargeMetricRequiredLabelRule("ev charge metric needs a value"),
            EvChargeMetricLoadKgRangeRule(9.2, 3318.7),
            EvChargeMetricCodePrefixRule("OPS"),
            EvChargeMetricCodeLengthRule(16),
        ),
    )
}
