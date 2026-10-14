package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.text.normalize

/** A rule a seasonal route offset threshold must pass. Check returns its problems, or none. */
interface SeasonalRouteOffsetThresholdRule {
    fun check(item: SeasonalRouteOffsetThreshold): MutableList<String>
}

data class SeasonalRouteOffsetThresholdRequiredChannelRule(
    var message: String,
) : SeasonalRouteOffsetThresholdRule {
    override fun check(item: SeasonalRouteOffsetThreshold): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetThresholdCodePrefixRule(var prefix: String) : SeasonalRouteOffsetThresholdRule {
    override fun check(item: SeasonalRouteOffsetThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetThresholdAllowedTitleRule(
    var allowed: MutableList<String>,
) : SeasonalRouteOffsetThresholdRule {
    override fun check(item: SeasonalRouteOffsetThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalRouteOffsetThresholdNotesLengthRule(var maxLength: Int) : SeasonalRouteOffsetThresholdRule {
    override fun check(item: SeasonalRouteOffsetThreshold): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal route offset threshold. */
data class SeasonalRouteOffsetThresholdValidator(var rules: MutableList<SeasonalRouteOffsetThresholdRule>) {
    fun validate(item: SeasonalRouteOffsetThreshold): MutableList<String> {
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

    fun requireValid(item: SeasonalRouteOffsetThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalRouteOffsetThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalRouteOffsetThresholdValidator(): SeasonalRouteOffsetThresholdValidator {
    return SeasonalRouteOffsetThresholdValidator(
        mutableListOf<SeasonalRouteOffsetThresholdRule>(
            SeasonalRouteOffsetThresholdRequiredChannelRule("seasonal route offset threshold needs an entry"),
            SeasonalRouteOffsetThresholdCodePrefixRule("OPS"),
            SeasonalRouteOffsetThresholdAllowedTitleRule(mutableListOf<String>("primary", "fallback")),
            SeasonalRouteOffsetThresholdNotesLengthRule(3600),
        ),
    )
}
