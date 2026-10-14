package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize

/** A rule a seasonal route offset segment must pass. Check returns its problems, or none. */
interface SeasonalRouteOffsetSegmentRule {
    fun check(item: SeasonalRouteOffsetSegment): MutableList<String>
}

data class SeasonalRouteOffsetSegmentCodePrefixRule(var prefix: String) : SeasonalRouteOffsetSegmentRule {
    override fun check(item: SeasonalRouteOffsetSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetSegmentLaneLengthRule(var maxLength: Int) : SeasonalRouteOffsetSegmentRule {
    override fun check(item: SeasonalRouteOffsetSegment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetSegmentAllowedLaneRule(
    var allowed: MutableList<String>,
) : SeasonalRouteOffsetSegmentRule {
    override fun check(item: SeasonalRouteOffsetSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal route offset segment. */
data class SeasonalRouteOffsetSegmentValidator(var rules: MutableList<SeasonalRouteOffsetSegmentRule>) {
    fun validate(item: SeasonalRouteOffsetSegment): MutableList<String> {
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

    fun requireValid(item: SeasonalRouteOffsetSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRouteOffsetSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRouteOffsetSegmentValidator(): SeasonalRouteOffsetSegmentValidator {
    return SeasonalRouteOffsetSegmentValidator(
        mutableListOf<SeasonalRouteOffsetSegmentRule>(
            SeasonalRouteOffsetSegmentCodePrefixRule("X"),
            SeasonalRouteOffsetSegmentLaneLengthRule(86400),
            SeasonalRouteOffsetSegmentAllowedLaneRule(mutableListOf<String>("strict", "regional")),
        ),
    )
}
