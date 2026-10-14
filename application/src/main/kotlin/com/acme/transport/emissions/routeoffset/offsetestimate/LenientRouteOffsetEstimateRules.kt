package com.acme.transport.emissions.routeoffset.offsetestimate

/** A rule a lenient route offset estimate must pass. Check returns its problems, or none. */
interface LenientRouteOffsetEstimateRule {
    fun check(item: LenientRouteOffsetEstimate): MutableList<String>
}

data class LenientRouteOffsetEstimateMaximumRangeRule(
    var min: Double,
    var max: Double,
) : LenientRouteOffsetEstimateRule {
    override fun check(item: LenientRouteOffsetEstimate): MutableList<String> {
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

data class LenientRouteOffsetEstimateCodePrefixRule(var prefix: String) : LenientRouteOffsetEstimateRule {
    override fun check(item: LenientRouteOffsetEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetEstimateLabelLengthRule(var maxLength: Int) : LenientRouteOffsetEstimateRule {
    override fun check(item: LenientRouteOffsetEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetEstimateRequiredLabelRule(var message: String) : LenientRouteOffsetEstimateRule {
    override fun check(item: LenientRouteOffsetEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient route offset estimate. */
data class LenientRouteOffsetEstimateValidator(var rules: MutableList<LenientRouteOffsetEstimateRule>) {
    fun validate(item: LenientRouteOffsetEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRouteOffsetEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRouteOffsetEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRouteOffsetEstimateValidator(): LenientRouteOffsetEstimateValidator {
    return LenientRouteOffsetEstimateValidator(
        mutableListOf<LenientRouteOffsetEstimateRule>(
            LenientRouteOffsetEstimateMaximumRangeRule(0.6, 1949.6),
            LenientRouteOffsetEstimateCodePrefixRule("ACM"),
            LenientRouteOffsetEstimateLabelLengthRule(16),
            LenientRouteOffsetEstimateRequiredLabelRule("lenient route offset estimate needs a value"),
        ),
    )
}
