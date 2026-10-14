package com.acme.transport.emissions.routeoffset.offsetcheckpoint

/** A rule a route offset preference must pass. Check returns its problems, or none. */
interface RouteOffsetPreferenceRule {
    fun check(item: RouteOffsetPreference): MutableList<String>
}

data class RouteOffsetPreferenceStopCountRangeRule(var min: Int, var max: Int) : RouteOffsetPreferenceRule {
    override fun check(item: RouteOffsetPreference): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class RouteOffsetPreferenceRequiredLabelRule(var message: String) : RouteOffsetPreferenceRule {
    override fun check(item: RouteOffsetPreference): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPreferenceDepotLengthRule(var maxLength: Int) : RouteOffsetPreferenceRule {
    override fun check(item: RouteOffsetPreference): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPreferenceContactEmailLengthRule(var maxLength: Int) : RouteOffsetPreferenceRule {
    override fun check(item: RouteOffsetPreference): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetPreferenceCodeLengthRule(var maxLength: Int) : RouteOffsetPreferenceRule {
    override fun check(item: RouteOffsetPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset preference. */
data class RouteOffsetPreferenceValidator(var rules: MutableList<RouteOffsetPreferenceRule>) {
    fun validate(item: RouteOffsetPreference): MutableList<String> {
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

    fun requireValid(item: RouteOffsetPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetPreferenceValidator(): RouteOffsetPreferenceValidator {
    return RouteOffsetPreferenceValidator(
        mutableListOf<RouteOffsetPreferenceRule>(
            RouteOffsetPreferenceStopCountRangeRule(64, 120),
            RouteOffsetPreferenceRequiredLabelRule("route offset preference needs an entry"),
            RouteOffsetPreferenceDepotLengthRule(86400),
            RouteOffsetPreferenceContactEmailLengthRule(86400),
            RouteOffsetPreferenceCodeLengthRule(32),
        ),
    )
}
