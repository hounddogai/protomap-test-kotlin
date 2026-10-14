package com.acme.transport.emissions.routeoffset.segment

/** A rule a route offset score must pass. Check returns its problems, or none. */
interface RouteOffsetScoreRule {
    fun check(item: RouteOffsetScore): MutableList<String>
}

data class RouteOffsetScoreDepotLengthRule(var maxLength: Int) : RouteOffsetScoreRule {
    override fun check(item: RouteOffsetScore): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScoreUpdatedAtRangeRule(var min: Int, var max: Int) : RouteOffsetScoreRule {
    override fun check(item: RouteOffsetScore): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScoreSequenceRangeRule(var min: Int, var max: Int) : RouteOffsetScoreRule {
    override fun check(item: RouteOffsetScore): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetScoreLabelLengthRule(var maxLength: Int) : RouteOffsetScoreRule {
    override fun check(item: RouteOffsetScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset score. */
data class RouteOffsetScoreValidator(var rules: MutableList<RouteOffsetScoreRule>) {
    fun validate(item: RouteOffsetScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetScoreValidator(): RouteOffsetScoreValidator {
    return RouteOffsetScoreValidator(
        mutableListOf<RouteOffsetScoreRule>(
            RouteOffsetScoreDepotLengthRule(86400),
            RouteOffsetScoreUpdatedAtRangeRule(32, 3600),
            RouteOffsetScoreSequenceRangeRule(16, 3600),
            RouteOffsetScoreLabelLengthRule(16),
        ),
    )
}
