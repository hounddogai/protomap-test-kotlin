package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.text.normalize

/** A rule a seasonal service plan preference must pass. Check returns its problems, or none. */
interface SeasonalServicePlanPreferenceRule {
    fun check(item: SeasonalServicePlanPreference): MutableList<String>
}

data class SeasonalServicePlanPreferenceWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalServicePlanPreferenceRule {
    override fun check(item: SeasonalServicePlanPreference): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class SeasonalServicePlanPreferenceCodePrefixRule(var prefix: String) : SeasonalServicePlanPreferenceRule {
    override fun check(item: SeasonalServicePlanPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanPreferenceRequiredLabelRule(
    var message: String,
) : SeasonalServicePlanPreferenceRule {
    override fun check(item: SeasonalServicePlanPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanPreferenceAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalServicePlanPreferenceRule {
    override fun check(item: SeasonalServicePlanPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalServicePlanPreferenceRouteCodeLengthRule(
    var maxLength: Int,
) : SeasonalServicePlanPreferenceRule {
    override fun check(item: SeasonalServicePlanPreference): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal service plan preference. */
data class SeasonalServicePlanPreferenceValidator(var rules: MutableList<SeasonalServicePlanPreferenceRule>) {
    fun validate(item: SeasonalServicePlanPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalServicePlanPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalServicePlanPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalServicePlanPreferenceValidator(): SeasonalServicePlanPreferenceValidator {
    return SeasonalServicePlanPreferenceValidator(
        mutableListOf<SeasonalServicePlanPreferenceRule>(
            SeasonalServicePlanPreferenceWindowDaysRangeRule(120, 255),
            SeasonalServicePlanPreferenceCodePrefixRule("OPS"),
            SeasonalServicePlanPreferenceRequiredLabelRule("seasonal service plan preference needs an entry"),
            SeasonalServicePlanPreferenceAllowedLabelRule(mutableListOf<String>("manual")),
            SeasonalServicePlanPreferenceRouteCodeLengthRule(86400),
        ),
    )
}
