package com.acme.transport.emissions.routeoffset.offsetnotice

/** A rule a default route offset baseline must pass. Check returns its problems, or none. */
interface DefaultRouteOffsetBaselineRule {
    fun check(item: DefaultRouteOffsetBaseline): MutableList<String>
}

data class DefaultRouteOffsetBaselineCodePrefixRule(var prefix: String) : DefaultRouteOffsetBaselineRule {
    override fun check(item: DefaultRouteOffsetBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetBaselineDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : DefaultRouteOffsetBaselineRule {
    override fun check(item: DefaultRouteOffsetBaseline): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRouteOffsetBaselineRequiredTitleRule(var message: String) : DefaultRouteOffsetBaselineRule {
    override fun check(item: DefaultRouteOffsetBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default route offset baseline. */
data class DefaultRouteOffsetBaselineValidator(var rules: MutableList<DefaultRouteOffsetBaselineRule>) {
    fun validate(item: DefaultRouteOffsetBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRouteOffsetBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRouteOffsetBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRouteOffsetBaselineValidator(): DefaultRouteOffsetBaselineValidator {
    return DefaultRouteOffsetBaselineValidator(
        mutableListOf<DefaultRouteOffsetBaselineRule>(
            DefaultRouteOffsetBaselineCodePrefixRule("REF"),
            DefaultRouteOffsetBaselineDurationMinutesRangeRule(3600, 86400),
            DefaultRouteOffsetBaselineRequiredTitleRule("default route offset baseline needs an entry"),
        ),
    )
}
