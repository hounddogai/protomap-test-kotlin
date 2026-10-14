package com.acme.transport.emissions.routeoffset.offsetbaseline

/** A rule a capped route offset baseline must pass. Check returns its problems, or none. */
interface CappedRouteOffsetBaselineRule {
    fun check(item: CappedRouteOffsetBaseline): MutableList<String>
}

data class CappedRouteOffsetBaselineRequiredCodeRule(var message: String) : CappedRouteOffsetBaselineRule {
    override fun check(item: CappedRouteOffsetBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetBaselineThresholdRangeRule(
    var min: Double,
    var max: Double,
) : CappedRouteOffsetBaselineRule {
    override fun check(item: CappedRouteOffsetBaseline): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class CappedRouteOffsetBaselineNameLengthRule(var maxLength: Int) : CappedRouteOffsetBaselineRule {
    override fun check(item: CappedRouteOffsetBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedRouteOffsetBaselineMaximumRangeRule(
    var min: Double,
    var max: Double,
) : CappedRouteOffsetBaselineRule {
    override fun check(item: CappedRouteOffsetBaseline): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped route offset baseline. */
data class CappedRouteOffsetBaselineValidator(var rules: MutableList<CappedRouteOffsetBaselineRule>) {
    fun validate(item: CappedRouteOffsetBaseline): MutableList<String> {
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

    fun requireValid(item: CappedRouteOffsetBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedRouteOffsetBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedRouteOffsetBaselineValidator(): CappedRouteOffsetBaselineValidator {
    return CappedRouteOffsetBaselineValidator(
        mutableListOf<CappedRouteOffsetBaselineRule>(
            CappedRouteOffsetBaselineRequiredCodeRule("capped route offset baseline needs a value"),
            CappedRouteOffsetBaselineThresholdRangeRule(9.1, 2367.6),
            CappedRouteOffsetBaselineNameLengthRule(1000),
            CappedRouteOffsetBaselineMaximumRangeRule(7.3, 495.7),
        ),
    )
}
