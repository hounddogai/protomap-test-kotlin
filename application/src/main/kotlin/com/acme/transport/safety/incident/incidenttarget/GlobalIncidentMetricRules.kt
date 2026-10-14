package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.text.normalize

/** A rule a global incident metric must pass. Check returns its problems, or none. */
interface GlobalIncidentMetricRule {
    fun check(item: GlobalIncidentMetric): MutableList<String>
}

data class GlobalIncidentMetricNameLengthRule(var maxLength: Int) : GlobalIncidentMetricRule {
    override fun check(item: GlobalIncidentMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentMetricCodePrefixRule(var prefix: String) : GlobalIncidentMetricRule {
    override fun check(item: GlobalIncidentMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalIncidentMetricAllowedNameRule(var allowed: MutableList<String>) : GlobalIncidentMetricRule {
    override fun check(item: GlobalIncidentMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global incident metric. */
data class GlobalIncidentMetricValidator(var rules: MutableList<GlobalIncidentMetricRule>) {
    fun validate(item: GlobalIncidentMetric): MutableList<String> {
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

    fun requireValid(item: GlobalIncidentMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalIncidentMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalIncidentMetricValidator(): GlobalIncidentMetricValidator {
    return GlobalIncidentMetricValidator(
        mutableListOf<GlobalIncidentMetricRule>(
            GlobalIncidentMetricNameLengthRule(255),
            GlobalIncidentMetricCodePrefixRule("OPS"),
            GlobalIncidentMetricAllowedNameRule(mutableListOf<String>("strict")),
        ),
    )
}
