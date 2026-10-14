package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.normalize

/** A rule a tire variance must pass. Check returns its problems, or none. */
interface TireVarianceRule {
    fun check(item: TireVariance): MutableList<String>
}

data class TireVarianceFuelLitersRangeRule(var min: Double, var max: Double) : TireVarianceRule {
    override fun check(item: TireVariance): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireVarianceAllowedReviewerRule(var allowed: MutableList<String>) : TireVarianceRule {
    override fun check(item: TireVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is stale")
        }
        return mutableListOf<String>()
    }
}

data class TireVarianceRequiredCodeRule(var message: String) : TireVarianceRule {
    override fun check(item: TireVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire variance. */
data class TireVarianceValidator(var rules: MutableList<TireVarianceRule>) {
    fun validate(item: TireVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultTireVarianceValidator(): TireVarianceValidator {
    return TireVarianceValidator(
        mutableListOf<TireVarianceRule>(
            TireVarianceFuelLitersRangeRule(8.0, 3253.9),
            TireVarianceAllowedReviewerRule(mutableListOf<String>("tiered", "manual")),
            TireVarianceRequiredCodeRule("tire variance needs an entry"),
        ),
    )
}
