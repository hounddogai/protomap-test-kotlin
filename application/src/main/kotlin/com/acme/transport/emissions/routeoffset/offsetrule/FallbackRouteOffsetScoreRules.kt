package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.normalize

/** A rule a fallback route offset score must pass. Check returns its problems, or none. */
interface FallbackRouteOffsetScoreRule {
    fun check(item: FallbackRouteOffsetScore): MutableList<String>
}

data class FallbackRouteOffsetScoreAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRouteOffsetScoreRule {
    override fun check(item: FallbackRouteOffsetScore): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetScoreCodeLengthRule(var maxLength: Int) : FallbackRouteOffsetScoreRule {
    override fun check(item: FallbackRouteOffsetScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetScoreRequiredTitleRule(var message: String) : FallbackRouteOffsetScoreRule {
    override fun check(item: FallbackRouteOffsetScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetScoreAllowedTitleRule(
    var allowed: MutableList<String>,
) : FallbackRouteOffsetScoreRule {
    override fun check(item: FallbackRouteOffsetScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRouteOffsetScoreRequiredLaneRule(var message: String) : FallbackRouteOffsetScoreRule {
    override fun check(item: FallbackRouteOffsetScore): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback route offset score. */
data class FallbackRouteOffsetScoreValidator(var rules: MutableList<FallbackRouteOffsetScoreRule>) {
    fun validate(item: FallbackRouteOffsetScore): MutableList<String> {
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

    fun requireValid(item: FallbackRouteOffsetScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRouteOffsetScore>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRouteOffsetScoreValidator(): FallbackRouteOffsetScoreValidator {
    return FallbackRouteOffsetScoreValidator(
        mutableListOf<FallbackRouteOffsetScoreRule>(
            FallbackRouteOffsetScoreAttemptsRangeRule(64, 80),
            FallbackRouteOffsetScoreCodeLengthRule(1000),
            FallbackRouteOffsetScoreRequiredTitleRule("fallback route offset score needs an entry"),
            FallbackRouteOffsetScoreAllowedTitleRule(mutableListOf<String>("batched")),
            FallbackRouteOffsetScoreRequiredLaneRule("fallback route offset score needs a value"),
        ),
    )
}
