package com.acme.transport.emissions.routeoffset.rule

/** A rule a fallback route offset baseline must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetBaselineRule {
    fun check(item: FallbackRouteOffsetBaseline): MutableList<String>
}

data class FallbackRouteOffsetBaselineNameLengthRule(var maxLength: Int) : FallbackRouteOffsetBaselineRule {
    override fun check(item: FallbackRouteOffsetBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetBaselineRequiredNameRule(var message: String) : FallbackRouteOffsetBaselineRule {
    override fun check(item: FallbackRouteOffsetBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetBaselineLabelLengthRule(var maxLength: Int) : FallbackRouteOffsetBaselineRule {
    override fun check(item: FallbackRouteOffsetBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetBaselineCodeLengthRule(var maxLength: Int) : FallbackRouteOffsetBaselineRule {
    override fun check(item: FallbackRouteOffsetBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetBaselineGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRouteOffsetBaselineRule {
    override fun check(item: FallbackRouteOffsetBaseline): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset baseline. */
data class FallbackRouteOffsetBaselineValidator(var rules: MutableList<FallbackRouteOffsetBaselineRule>) {
    fun validate(item: FallbackRouteOffsetBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackRouteOffsetBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetBaselineValidator(): FallbackRouteOffsetBaselineValidator {
    return FallbackRouteOffsetBaselineValidator(
        mutableListOf<FallbackRouteOffsetBaselineRule>(
            FallbackRouteOffsetBaselineNameLengthRule(255),
            FallbackRouteOffsetBaselineRequiredNameRule("fallback route offset baseline needs an entry"),
            FallbackRouteOffsetBaselineLabelLengthRule(120),
            FallbackRouteOffsetBaselineCodeLengthRule(120),
            FallbackRouteOffsetBaselineGraceDaysRangeRule(16, 3600),
        ),
    )
}
