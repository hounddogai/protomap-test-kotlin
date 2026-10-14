package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.normalize

/** A rule a strict service area bundle must pass. Check returns its problems, or none. */
interface StrictServiceAreaBundleRule {
    fun check(item: StrictServiceAreaBundle): MutableList<String>
}

data class StrictServiceAreaBundleRequiredReviewerRule(var message: String) : StrictServiceAreaBundleRule {
    override fun check(item: StrictServiceAreaBundle): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaBundleReviewerLengthRule(var maxLength: Int) : StrictServiceAreaBundleRule {
    override fun check(item: StrictServiceAreaBundle): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaBundleAllowedCodeRule(
    var allowed: MutableList<String>,
) : StrictServiceAreaBundleRule {
    override fun check(item: StrictServiceAreaBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaBundleLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : StrictServiceAreaBundleRule {
    override fun check(item: StrictServiceAreaBundle): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictServiceAreaBundlePriorityRangeRule(var min: Int, var max: Int) : StrictServiceAreaBundleRule {
    override fun check(item: StrictServiceAreaBundle): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class StrictServiceAreaBundleCodeLengthRule(var maxLength: Int) : StrictServiceAreaBundleRule {
    override fun check(item: StrictServiceAreaBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict service area bundle. */
data class StrictServiceAreaBundleValidator(var rules: MutableList<StrictServiceAreaBundleRule>) {
    fun validate(item: StrictServiceAreaBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictServiceAreaBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictServiceAreaBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictServiceAreaBundleValidator(): StrictServiceAreaBundleValidator {
    return StrictServiceAreaBundleValidator(
        mutableListOf<StrictServiceAreaBundleRule>(
            StrictServiceAreaBundleRequiredReviewerRule("strict service area bundle needs a value"),
            StrictServiceAreaBundleReviewerLengthRule(16),
            StrictServiceAreaBundleAllowedCodeRule(mutableListOf<String>("global", "global", "scheduled")),
            StrictServiceAreaBundleLoadKgRangeRule(0.4, 291.1),
            StrictServiceAreaBundlePriorityRangeRule(64, 255),
            StrictServiceAreaBundleCodeLengthRule(3600),
        ),
    )
}
