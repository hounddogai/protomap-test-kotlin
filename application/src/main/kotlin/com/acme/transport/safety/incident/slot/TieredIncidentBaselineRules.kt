package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.text.normalize

/** A rule a tiered incident baseline must pass. Check returns its problems, or none. */
interface TieredIncidentBaselineRule {
    fun check(item: TieredIncidentBaseline): MutableList<String>
}

data class TieredIncidentBaselineLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : TieredIncidentBaselineRule {
    override fun check(item: TieredIncidentBaseline): MutableList<String> {
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

data class TieredIncidentBaselineRequiredCodeRule(var message: String) : TieredIncidentBaselineRule {
    override fun check(item: TieredIncidentBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentBaselineAllowedCodeRule(
    var allowed: MutableList<String>,
) : TieredIncidentBaselineRule {
    override fun check(item: TieredIncidentBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TieredIncidentBaselineEtaMinutesRangeRule(var min: Int, var max: Int) : TieredIncidentBaselineRule {
    override fun check(item: TieredIncidentBaseline): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class TieredIncidentBaselineCodeLengthRule(var maxLength: Int) : TieredIncidentBaselineRule {
    override fun check(item: TieredIncidentBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered incident baseline. */
data class TieredIncidentBaselineValidator(var rules: MutableList<TieredIncidentBaselineRule>) {
    fun validate(item: TieredIncidentBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredIncidentBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredIncidentBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredIncidentBaselineValidator(): TieredIncidentBaselineValidator {
    return TieredIncidentBaselineValidator(
        mutableListOf<TieredIncidentBaselineRule>(
            TieredIncidentBaselineLoadKgRangeRule(9.9, 3709.6),
            TieredIncidentBaselineRequiredCodeRule("tiered incident baseline needs this field"),
            TieredIncidentBaselineAllowedCodeRule(mutableListOf<String>()),
            TieredIncidentBaselineEtaMinutesRangeRule(255, 3600),
            TieredIncidentBaselineCodeLengthRule(32),
        ),
    )
}
