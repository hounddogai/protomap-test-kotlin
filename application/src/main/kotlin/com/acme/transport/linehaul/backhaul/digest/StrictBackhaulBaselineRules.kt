package com.acme.transport.linehaul.backhaul.digest

import com.acme.transport.kit.text.normalize

/** A rule a strict backhaul baseline must pass. Check returns its problems, or none. */
interface StrictBackhaulBaselineRule {
    fun check(item: StrictBackhaulBaseline): MutableList<String>
}

data class StrictBackhaulBaselineRequiredExternalRefRule(var message: String) : StrictBackhaulBaselineRule {
    override fun check(item: StrictBackhaulBaseline): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulBaselineCodeLengthRule(var maxLength: Int) : StrictBackhaulBaselineRule {
    override fun check(item: StrictBackhaulBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulBaselineRequiredLabelRule(var message: String) : StrictBackhaulBaselineRule {
    override fun check(item: StrictBackhaulBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulBaselineRateRangeRule(var min: Double, var max: Double) : StrictBackhaulBaselineRule {
    override fun check(item: StrictBackhaulBaseline): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulBaselineAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictBackhaulBaselineRule {
    override fun check(item: StrictBackhaulBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class StrictBackhaulBaselineExpiresAtRangeRule(var min: Int, var max: Int) : StrictBackhaulBaselineRule {
    override fun check(item: StrictBackhaulBaseline): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict backhaul baseline. */
data class StrictBackhaulBaselineValidator(var rules: MutableList<StrictBackhaulBaselineRule>) {
    fun validate(item: StrictBackhaulBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictBackhaulBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBackhaulBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBackhaulBaselineValidator(): StrictBackhaulBaselineValidator {
    return StrictBackhaulBaselineValidator(
        mutableListOf<StrictBackhaulBaselineRule>(
            StrictBackhaulBaselineRequiredExternalRefRule("strict backhaul baseline needs an entry"),
            StrictBackhaulBaselineCodeLengthRule(1000),
            StrictBackhaulBaselineRequiredLabelRule("strict backhaul baseline needs this field"),
            StrictBackhaulBaselineRateRangeRule(1.5, 2713.7),
            StrictBackhaulBaselineAllowedCodeRule(mutableListOf<String>("pending")),
            StrictBackhaulBaselineExpiresAtRangeRule(64, 3600),
        ),
    )
}
