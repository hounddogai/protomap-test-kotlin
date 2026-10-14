package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.text.normalize

/** A rule a regional ev charge metric must pass. Check returns its problems, or none. */
interface RegionalEvChargeMetricRule {
    fun check(item: RegionalEvChargeMetric): MutableList<String>
}

data class RegionalEvChargeMetricLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalEvChargeMetricRule {
    override fun check(item: RegionalEvChargeMetric): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeMetricDescriptionLengthRule(var maxLength: Int) : RegionalEvChargeMetricRule {
    override fun check(item: RegionalEvChargeMetric): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeMetricCodePrefixRule(var prefix: String) : RegionalEvChargeMetricRule {
    override fun check(item: RegionalEvChargeMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeMetricCodeLengthRule(var maxLength: Int) : RegionalEvChargeMetricRule {
    override fun check(item: RegionalEvChargeMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalEvChargeMetricAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : RegionalEvChargeMetricRule {
    override fun check(item: RegionalEvChargeMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional ev charge metric. */
data class RegionalEvChargeMetricValidator(var rules: MutableList<RegionalEvChargeMetricRule>) {
    fun validate(item: RegionalEvChargeMetric): MutableList<String> {
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

    fun requireValid(item: RegionalEvChargeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalEvChargeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalEvChargeMetricValidator(): RegionalEvChargeMetricValidator {
    return RegionalEvChargeMetricValidator(
        mutableListOf<RegionalEvChargeMetricRule>(
            RegionalEvChargeMetricLoadKgRangeRule(8.7, 3490.8),
            RegionalEvChargeMetricDescriptionLengthRule(86400),
            RegionalEvChargeMetricCodePrefixRule("REF"),
            RegionalEvChargeMetricCodeLengthRule(86400),
            RegionalEvChargeMetricAllowedVehicleIdRule(mutableListOf<String>()),
        ),
    )
}
