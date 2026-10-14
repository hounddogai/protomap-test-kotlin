package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.text.normalize

/** A rule a fallback route offset profile must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetProfileRule {
    fun check(item: FallbackRouteOffsetProfile): MutableList<String>
}

data class FallbackRouteOffsetProfileAllowedRouteCodeRule(
    var allowed: MutableList<String>,
) : FallbackRouteOffsetProfileRule {
    override fun check(item: FallbackRouteOffsetProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetProfileRequiredCodeRule(var message: String) : FallbackRouteOffsetProfileRule {
    override fun check(item: FallbackRouteOffsetProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetProfileTitleLengthRule(var maxLength: Int) : FallbackRouteOffsetProfileRule {
    override fun check(item: FallbackRouteOffsetProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset profile. */
data class FallbackRouteOffsetProfileValidator(var rules: MutableList<FallbackRouteOffsetProfileRule>) {
    fun validate(item: FallbackRouteOffsetProfile): MutableList<String> {
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

    fun requireValid(item: FallbackRouteOffsetProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetProfileValidator(): FallbackRouteOffsetProfileValidator {
    return FallbackRouteOffsetProfileValidator(
        mutableListOf<FallbackRouteOffsetProfileRule>(
            FallbackRouteOffsetProfileAllowedRouteCodeRule(mutableListOf<String>()),
            FallbackRouteOffsetProfileRequiredCodeRule("fallback route offset profile needs a value"),
            FallbackRouteOffsetProfileTitleLengthRule(32),
        ),
    )
}
