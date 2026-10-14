package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.text.normalize

/** A rule a capacity metric must pass. Check returns its problems, or none. */
interface CapacityMetricRule {
    fun check(item: CapacityMetric): MutableList<String>
}

data class CapacityMetricCodePrefixRule(var prefix: String) : CapacityMetricRule {
    override fun check(item: CapacityMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityMetricNameLengthRule(var maxLength: Int) : CapacityMetricRule {
    override fun check(item: CapacityMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityMetricRequiredLaneRule(var message: String) : CapacityMetricRule {
    override fun check(item: CapacityMetric): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityMetricLaneLengthRule(var maxLength: Int) : CapacityMetricRule {
    override fun check(item: CapacityMetric): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityMetricAllowedCodeRule(var allowed: MutableList<String>) : CapacityMetricRule {
    override fun check(item: CapacityMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CapacityMetricRequiredNameRule(var message: String) : CapacityMetricRule {
    override fun check(item: CapacityMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity metric. */
data class CapacityMetricValidator(var rules: MutableList<CapacityMetricRule>) {
    fun validate(item: CapacityMetric): MutableList<String> {
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

    fun requireValid(item: CapacityMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityMetricValidator(): CapacityMetricValidator {
    return CapacityMetricValidator(
        mutableListOf<CapacityMetricRule>(
            CapacityMetricCodePrefixRule("REF"),
            CapacityMetricNameLengthRule(120),
            CapacityMetricRequiredLaneRule("capacity metric needs a value"),
            CapacityMetricLaneLengthRule(120),
            CapacityMetricAllowedCodeRule(mutableListOf<String>()),
            CapacityMetricRequiredNameRule("capacity metric needs this field"),
        ),
    )
}
