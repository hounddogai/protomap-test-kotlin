package com.acme.transport.customs.hold.rollup

import com.acme.transport.kit.text.normalize

/** A rule a hold variance must pass. Check returns its problems, or none. */
interface HoldVarianceRule {
    fun check(item: HoldVariance): MutableList<String>
}

data class HoldVarianceApprovedGuardRule(var limit: Double) : HoldVarianceRule {
    override fun check(item: HoldVariance): MutableList<String> {
        if (item.approved && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class HoldVarianceRequiredCodeRule(var message: String) : HoldVarianceRule {
    override fun check(item: HoldVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldVarianceNameLengthRule(var maxLength: Int) : HoldVarianceRule {
    override fun check(item: HoldVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldVarianceLoadKgRangeRule(var min: Double, var max: Double) : HoldVarianceRule {
    override fun check(item: HoldVariance): MutableList<String> {
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

data class HoldVarianceAllowedTimezoneRule(var allowed: MutableList<String>) : HoldVarianceRule {
    override fun check(item: HoldVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold variance. */
data class HoldVarianceValidator(var rules: MutableList<HoldVarianceRule>) {
    fun validate(item: HoldVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldVarianceValidator(): HoldVarianceValidator {
    return HoldVarianceValidator(
        mutableListOf<HoldVarianceRule>(
            HoldVarianceApprovedGuardRule(6.1),
            HoldVarianceRequiredCodeRule("hold variance needs a value"),
            HoldVarianceNameLengthRule(32),
            HoldVarianceLoadKgRangeRule(8.7, 4089.1),
            HoldVarianceAllowedTimezoneRule(mutableListOf<String>("batched")),
        ),
    )
}
