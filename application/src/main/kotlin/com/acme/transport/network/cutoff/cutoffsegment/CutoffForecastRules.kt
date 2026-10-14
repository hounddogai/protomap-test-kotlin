package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.normalize

/** A rule a cutoff forecast must pass. Check returns its problems, or none. */
interface CutoffForecastRule {
    fun check(item: CutoffForecast): MutableList<String>
}

data class CutoffForecastAllowedTrailerIdRule(var allowed: MutableList<String>) : CutoffForecastRule {
    override fun check(item: CutoffForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CutoffForecastDepotLengthRule(var maxLength: Int) : CutoffForecastRule {
    override fun check(item: CutoffForecast): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffForecastRequiredCodeRule(var message: String) : CutoffForecastRule {
    override fun check(item: CutoffForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff forecast. */
data class CutoffForecastValidator(var rules: MutableList<CutoffForecastRule>) {
    fun validate(item: CutoffForecast): MutableList<String> {
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

    fun requireValid(item: CutoffForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffForecastValidator(): CutoffForecastValidator {
    return CutoffForecastValidator(
        mutableListOf<CutoffForecastRule>(
            CutoffForecastAllowedTrailerIdRule(mutableListOf<String>()),
            CutoffForecastDepotLengthRule(1000),
            CutoffForecastRequiredCodeRule("cutoff forecast needs a value"),
        ),
    )
}
