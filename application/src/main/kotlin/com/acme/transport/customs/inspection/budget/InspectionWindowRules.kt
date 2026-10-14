package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.text.normalize

/** A rule a inspection window must pass. Check returns its problems, or none. */
interface InspectionWindowRule {
    fun check(item: InspectionWindow): MutableList<String>
}

data class InspectionWindowAllowedLabelRule(var allowed: MutableList<String>) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class InspectionWindowCodePrefixRule(var prefix: String) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionWindowTrailerIdLengthRule(var maxLength: Int) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionWindowWindowDaysRangeRule(var min: Int, var max: Int) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
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

data class InspectionWindowExpiresAtRangeRule(var min: Int, var max: Int) : InspectionWindowRule {
    override fun check(item: InspectionWindow): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection window. */
data class InspectionWindowValidator(var rules: MutableList<InspectionWindowRule>) {
    fun validate(item: InspectionWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionWindowValidator(): InspectionWindowValidator {
    return InspectionWindowValidator(
        mutableListOf<InspectionWindowRule>(
            InspectionWindowAllowedLabelRule(mutableListOf<String>("weighted", "fallback", "default")),
            InspectionWindowCodePrefixRule("REF"),
            InspectionWindowTrailerIdLengthRule(1000),
            InspectionWindowWindowDaysRangeRule(80, 1000),
            InspectionWindowExpiresAtRangeRule(16, 1000),
        ),
    )
}
