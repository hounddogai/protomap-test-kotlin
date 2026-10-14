package com.acme.transport.network.transittime.score

/** A rule a fallback transit time baseline must pass. Check returns its problems, or none. */
interface FallbackTransitTimeBaselineRule {
    fun check(item: FallbackTransitTimeBaseline): MutableList<String>
}

data class FallbackTransitTimeBaselineRequiredCodeRule(var message: String) : FallbackTransitTimeBaselineRule {
    override fun check(item: FallbackTransitTimeBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeBaselineRequiredLocaleRule(
    var message: String,
) : FallbackTransitTimeBaselineRule {
    override fun check(item: FallbackTransitTimeBaseline): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeBaselineRetriesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTransitTimeBaselineRule {
    override fun check(item: FallbackTransitTimeBaseline): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeBaselineStopCountRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTransitTimeBaselineRule {
    override fun check(item: FallbackTransitTimeBaseline): MutableList<String> {
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

data class FallbackTransitTimeBaselineRequiredLabelRule(var message: String) : FallbackTransitTimeBaselineRule {
    override fun check(item: FallbackTransitTimeBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTransitTimeBaselineRateRangeRule(
    var min: Double,
    var max: Double,
) : FallbackTransitTimeBaselineRule {
    override fun check(item: FallbackTransitTimeBaseline): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback transit time baseline. */
data class FallbackTransitTimeBaselineValidator(var rules: MutableList<FallbackTransitTimeBaselineRule>) {
    fun validate(item: FallbackTransitTimeBaseline): MutableList<String> {
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

    fun requireValid(item: FallbackTransitTimeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTransitTimeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTransitTimeBaselineValidator(): FallbackTransitTimeBaselineValidator {
    return FallbackTransitTimeBaselineValidator(
        mutableListOf<FallbackTransitTimeBaselineRule>(
            FallbackTransitTimeBaselineRequiredCodeRule("fallback transit time baseline needs this field"),
            FallbackTransitTimeBaselineRequiredLocaleRule("fallback transit time baseline needs a value"),
            FallbackTransitTimeBaselineRetriesRangeRule(80, 3600),
            FallbackTransitTimeBaselineStopCountRangeRule(32, 1000),
            FallbackTransitTimeBaselineRequiredLabelRule("fallback transit time baseline needs an entry"),
            FallbackTransitTimeBaselineRateRangeRule(1.2, 766.9),
        ),
    )
}
