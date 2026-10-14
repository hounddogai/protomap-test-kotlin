package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.text.normalize

/** A rule a lenient clearance metric must pass. Check returns its problems, or none. */
interface LenientClearanceMetricRule {
    fun check(item: LenientClearanceMetric): MutableList<String>
}

data class LenientClearanceMetricCreatedAtRangeRule(var min: Int, var max: Int) : LenientClearanceMetricRule {
    override fun check(item: LenientClearanceMetric): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceMetricRequiredCountryRule(var message: String) : LenientClearanceMetricRule {
    override fun check(item: LenientClearanceMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceMetricRequiredCodeRule(var message: String) : LenientClearanceMetricRule {
    override fun check(item: LenientClearanceMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceMetricAllowedTitleRule(
    var allowed: MutableList<String>,
) : LenientClearanceMetricRule {
    override fun check(item: LenientClearanceMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceMetricTitleLengthRule(var maxLength: Int) : LenientClearanceMetricRule {
    override fun check(item: LenientClearanceMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceMetricCodePrefixRule(var prefix: String) : LenientClearanceMetricRule {
    override fun check(item: LenientClearanceMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient clearance metric. */
data class LenientClearanceMetricValidator(var rules: MutableList<LenientClearanceMetricRule>) {
    fun validate(item: LenientClearanceMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientClearanceMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientClearanceMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientClearanceMetricValidator(): LenientClearanceMetricValidator {
    return LenientClearanceMetricValidator(
        mutableListOf<LenientClearanceMetricRule>(
            LenientClearanceMetricCreatedAtRangeRule(3600, 86400),
            LenientClearanceMetricRequiredCountryRule("lenient clearance metric needs this field"),
            LenientClearanceMetricRequiredCodeRule("lenient clearance metric needs an entry"),
            LenientClearanceMetricAllowedTitleRule(mutableListOf<String>("rolling")),
            LenientClearanceMetricTitleLengthRule(255),
            LenientClearanceMetricCodePrefixRule("X"),
        ),
    )
}
