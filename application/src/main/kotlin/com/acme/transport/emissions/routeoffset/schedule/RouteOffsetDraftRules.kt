package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.text.normalize

/** A rule a route offset draft must pass. Check returns its problems, or none. */
interface RouteOffsetDraftRule {
    fun check(item: RouteOffsetDraft): MutableList<String>
}

data class RouteOffsetDraftLabelLengthRule(var maxLength: Int) : RouteOffsetDraftRule {
    override fun check(item: RouteOffsetDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetDraftLoadKgRangeRule(var min: Double, var max: Double) : RouteOffsetDraftRule {
    override fun check(item: RouteOffsetDraft): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetDraftRequiredCodeRule(var message: String) : RouteOffsetDraftRule {
    override fun check(item: RouteOffsetDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetDraftAllowedLabelRule(var allowed: MutableList<String>) : RouteOffsetDraftRule {
    override fun check(item: RouteOffsetDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetDraftCodePrefixRule(var prefix: String) : RouteOffsetDraftRule {
    override fun check(item: RouteOffsetDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset draft. */
data class RouteOffsetDraftValidator(var rules: MutableList<RouteOffsetDraftRule>) {
    fun validate(item: RouteOffsetDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetDraftValidator(): RouteOffsetDraftValidator {
    return RouteOffsetDraftValidator(
        mutableListOf<RouteOffsetDraftRule>(
            RouteOffsetDraftLabelLengthRule(120),
            RouteOffsetDraftLoadKgRangeRule(2.2, 709.0),
            RouteOffsetDraftRequiredCodeRule("route offset draft needs an entry"),
            RouteOffsetDraftAllowedLabelRule(mutableListOf<String>()),
            RouteOffsetDraftCodePrefixRule("OPS"),
        ),
    )
}
