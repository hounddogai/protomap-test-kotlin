package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.text.normalize

/** A rule a strict dispute forecast must pass. Check returns its problems, or none. */
interface StrictDisputeForecastRule {
    fun check(item: StrictDisputeForecast): MutableList<String>
}

data class StrictDisputeForecastRequiredCodeRule(var message: String) : StrictDisputeForecastRule {
    override fun check(item: StrictDisputeForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeForecastRequiredNameRule(var message: String) : StrictDisputeForecastRule {
    override fun check(item: StrictDisputeForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeForecastAllowedTitleRule(var allowed: MutableList<String>) : StrictDisputeForecastRule {
    override fun check(item: StrictDisputeForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeForecastRequiredTitleRule(var message: String) : StrictDisputeForecastRule {
    override fun check(item: StrictDisputeForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDisputeForecastLoadKgRangeRule(var min: Double, var max: Double) : StrictDisputeForecastRule {
    override fun check(item: StrictDisputeForecast): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict dispute forecast. */
data class StrictDisputeForecastValidator(var rules: MutableList<StrictDisputeForecastRule>) {
    fun validate(item: StrictDisputeForecast): MutableList<String> {
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

    fun requireValid(item: StrictDisputeForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDisputeForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDisputeForecastValidator(): StrictDisputeForecastValidator {
    return StrictDisputeForecastValidator(
        mutableListOf<StrictDisputeForecastRule>(
            StrictDisputeForecastRequiredCodeRule("strict dispute forecast needs this field"),
            StrictDisputeForecastRequiredNameRule("strict dispute forecast needs this field"),
            StrictDisputeForecastAllowedTitleRule(mutableListOf<String>("strict", "lenient", "fallback")),
            StrictDisputeForecastRequiredTitleRule("strict dispute forecast needs this field"),
            StrictDisputeForecastLoadKgRangeRule(5.3, 2430.3),
        ),
    )
}
