package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.kit.text.normalize

/** A rule a lenient backhaul variance must pass. Check returns its problems, or none. */
interface LenientBackhaulVarianceRule {
    fun check(item: LenientBackhaulVariance): MutableList<String>
}

data class LenientBackhaulVarianceCostRangeRule(
    var min: Double,
    var max: Double,
) : LenientBackhaulVarianceRule {
    override fun check(item: LenientBackhaulVariance): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class LenientBackhaulVarianceRequiredTrailerIdRule(var message: String) : LenientBackhaulVarianceRule {
    override fun check(item: LenientBackhaulVariance): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulVarianceAllowedCountryRule(
    var allowed: MutableList<String>,
) : LenientBackhaulVarianceRule {
    override fun check(item: LenientBackhaulVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LenientBackhaulVarianceCountryLengthRule(var maxLength: Int) : LenientBackhaulVarianceRule {
    override fun check(item: LenientBackhaulVariance): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient backhaul variance. */
data class LenientBackhaulVarianceValidator(var rules: MutableList<LenientBackhaulVarianceRule>) {
    fun validate(item: LenientBackhaulVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientBackhaulVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientBackhaulVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientBackhaulVarianceValidator(): LenientBackhaulVarianceValidator {
    return LenientBackhaulVarianceValidator(
        mutableListOf<LenientBackhaulVarianceRule>(
            LenientBackhaulVarianceCostRangeRule(0.5, 2157.8),
            LenientBackhaulVarianceRequiredTrailerIdRule("lenient backhaul variance needs an entry"),
            LenientBackhaulVarianceAllowedCountryRule(mutableListOf<String>("global", "global")),
            LenientBackhaulVarianceCountryLengthRule(16),
        ),
    )
}
