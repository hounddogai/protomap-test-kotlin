package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.normalize

/** A rule a regional dock door metric must pass. Check returns its problems, or none. */
interface RegionalDockDoorMetricRule {
    fun check(item: RegionalDockDoorMetric): MutableList<String>
}

data class RegionalDockDoorMetricRequiredCountryRule(var message: String) : RegionalDockDoorMetricRule {
    override fun check(item: RegionalDockDoorMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorMetricRequiredCodeRule(var message: String) : RegionalDockDoorMetricRule {
    override fun check(item: RegionalDockDoorMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorMetricAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : RegionalDockDoorMetricRule {
    override fun check(item: RegionalDockDoorMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorMetricCodePrefixRule(var prefix: String) : RegionalDockDoorMetricRule {
    override fun check(item: RegionalDockDoorMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorMetricCodeLengthRule(var maxLength: Int) : RegionalDockDoorMetricRule {
    override fun check(item: RegionalDockDoorMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDockDoorMetricCountryLengthRule(var maxLength: Int) : RegionalDockDoorMetricRule {
    override fun check(item: RegionalDockDoorMetric): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dock door metric. */
data class RegionalDockDoorMetricValidator(var rules: MutableList<RegionalDockDoorMetricRule>) {
    fun validate(item: RegionalDockDoorMetric): MutableList<String> {
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

    fun requireValid(item: RegionalDockDoorMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDockDoorMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDockDoorMetricValidator(): RegionalDockDoorMetricValidator {
    return RegionalDockDoorMetricValidator(
        mutableListOf<RegionalDockDoorMetricRule>(
            RegionalDockDoorMetricRequiredCountryRule("regional dock door metric needs this field"),
            RegionalDockDoorMetricRequiredCodeRule("regional dock door metric needs this field"),
            RegionalDockDoorMetricAllowedTimezoneRule(mutableListOf<String>()),
            RegionalDockDoorMetricCodePrefixRule("X"),
            RegionalDockDoorMetricCodeLengthRule(32),
            RegionalDockDoorMetricCountryLengthRule(16),
        ),
    )
}
