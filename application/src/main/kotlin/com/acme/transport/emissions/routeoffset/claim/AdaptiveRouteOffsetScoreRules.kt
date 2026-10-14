package com.acme.transport.emissions.routeoffset.claim

/** A rule a adaptive route offset score must pass. Check returns its problems, or none. */
interface AdaptiveRouteOffsetScoreRule {
    fun check(item: AdaptiveRouteOffsetScore): MutableList<String>
}

data class AdaptiveRouteOffsetScoreCodePrefixRule(var prefix: String) : AdaptiveRouteOffsetScoreRule {
    override fun check(item: AdaptiveRouteOffsetScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetScoreRequiredNameRule(var message: String) : AdaptiveRouteOffsetScoreRule {
    override fun check(item: AdaptiveRouteOffsetScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetScoreNameLengthRule(var maxLength: Int) : AdaptiveRouteOffsetScoreRule {
    override fun check(item: AdaptiveRouteOffsetScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveRouteOffsetScoreRequiredSummaryRule(var message: String) : AdaptiveRouteOffsetScoreRule {
    override fun check(item: AdaptiveRouteOffsetScore): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive route offset score. */
data class AdaptiveRouteOffsetScoreValidator(var rules: MutableList<AdaptiveRouteOffsetScoreRule>) {
    fun validate(item: AdaptiveRouteOffsetScore): MutableList<String> {
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

    fun requireValid(item: AdaptiveRouteOffsetScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveRouteOffsetScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveRouteOffsetScoreValidator(): AdaptiveRouteOffsetScoreValidator {
    return AdaptiveRouteOffsetScoreValidator(
        mutableListOf<AdaptiveRouteOffsetScoreRule>(
            AdaptiveRouteOffsetScoreCodePrefixRule("OPS"),
            AdaptiveRouteOffsetScoreRequiredNameRule("adaptive route offset score needs this field"),
            AdaptiveRouteOffsetScoreNameLengthRule(1000),
            AdaptiveRouteOffsetScoreRequiredSummaryRule("adaptive route offset score needs this field"),
        ),
    )
}
