package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.text.normalize

/** A rule a seasonal trailer yard override must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardOverrideRule {
    fun check(item: SeasonalTrailerYardOverride): MutableList<String>
}

data class SeasonalTrailerYardOverrideAllowedCodeRule(
    var allowed: MutableList<String>,
) : SeasonalTrailerYardOverrideRule {
    override fun check(item: SeasonalTrailerYardOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardOverrideCeilingRangeRule(
    var min: Double,
    var max: Double,
) : SeasonalTrailerYardOverrideRule {
    override fun check(item: SeasonalTrailerYardOverride): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class SeasonalTrailerYardOverrideRequiredNameRule(var message: String) : SeasonalTrailerYardOverrideRule {
    override fun check(item: SeasonalTrailerYardOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer yard override. */
data class SeasonalTrailerYardOverrideValidator(var rules: MutableList<SeasonalTrailerYardOverrideRule>) {
    fun validate(item: SeasonalTrailerYardOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalTrailerYardOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardOverrideValidator(): SeasonalTrailerYardOverrideValidator {
    return SeasonalTrailerYardOverrideValidator(
        mutableListOf<SeasonalTrailerYardOverrideRule>(
            SeasonalTrailerYardOverrideAllowedCodeRule(mutableListOf<String>("primary", "seasonal", "batched")),
            SeasonalTrailerYardOverrideCeilingRangeRule(3.1, 3889.6),
            SeasonalTrailerYardOverrideRequiredNameRule("seasonal trailer yard override needs an entry"),
        ),
    )
}
