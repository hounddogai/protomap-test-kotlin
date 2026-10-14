package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.text.normalize

/** A rule a route offset summary must pass. Check returns its problems, or none. */
interface RouteOffsetSummaryRule {
    fun check(item: RouteOffsetSummary): MutableList<String>
}

data class RouteOffsetSummaryRequiredTitleRule(var message: String) : RouteOffsetSummaryRule {
    override fun check(item: RouteOffsetSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSummaryRequiredCodeRule(var message: String) : RouteOffsetSummaryRule {
    override fun check(item: RouteOffsetSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSummaryRequiredReviewerRule(var message: String) : RouteOffsetSummaryRule {
    override fun check(item: RouteOffsetSummary): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSummaryBatchSizeRangeRule(var min: Int, var max: Int) : RouteOffsetSummaryRule {
    override fun check(item: RouteOffsetSummary): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetSummaryAllowedTitleRule(var allowed: MutableList<String>) : RouteOffsetSummaryRule {
    override fun check(item: RouteOffsetSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset summary. */
data class RouteOffsetSummaryValidator(var rules: MutableList<RouteOffsetSummaryRule>) {
    fun validate(item: RouteOffsetSummary): MutableList<String> {
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

    fun requireValid(item: RouteOffsetSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetSummaryValidator(): RouteOffsetSummaryValidator {
    return RouteOffsetSummaryValidator(
        mutableListOf<RouteOffsetSummaryRule>(
            RouteOffsetSummaryRequiredTitleRule("route offset summary needs an entry"),
            RouteOffsetSummaryRequiredCodeRule("route offset summary needs this field"),
            RouteOffsetSummaryRequiredReviewerRule("route offset summary needs this field"),
            RouteOffsetSummaryBatchSizeRangeRule(32, 32),
            RouteOffsetSummaryAllowedTitleRule(mutableListOf<String>("pending")),
        ),
    )
}
