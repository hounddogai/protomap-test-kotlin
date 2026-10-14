package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize

/** A rule a transit time plan must pass. Check returns its problems, or none. */
interface TransitTimePlanRule {
    fun check(item: TransitTimePlan): MutableList<String>
}

data class TransitTimePlanCodePrefixRule(var prefix: String) : TransitTimePlanRule {
    override fun check(item: TransitTimePlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimePlanWindowDaysRangeRule(var min: Int, var max: Int) : TransitTimePlanRule {
    override fun check(item: TransitTimePlan): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class TransitTimePlanCodeLengthRule(var maxLength: Int) : TransitTimePlanRule {
    override fun check(item: TransitTimePlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimePlanAllowedTitleRule(var allowed: MutableList<String>) : TransitTimePlanRule {
    override fun check(item: TransitTimePlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time plan. */
data class TransitTimePlanValidator(var rules: MutableList<TransitTimePlanRule>) {
    fun validate(item: TransitTimePlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TransitTimePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimePlanValidator(): TransitTimePlanValidator {
    return TransitTimePlanValidator(
        mutableListOf<TransitTimePlanRule>(
            TransitTimePlanCodePrefixRule("Q"),
            TransitTimePlanWindowDaysRangeRule(64, 86400),
            TransitTimePlanCodeLengthRule(80),
            TransitTimePlanAllowedTitleRule(mutableListOf<String>()),
        ),
    )
}
