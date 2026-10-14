package com.acme.transport.emissions.routeoffset.rule

/** A rule a route offset rule must pass. Check returns its problems, or none. */
interface RouteOffsetRuleRule {
    fun check(item: RouteOffsetRule): MutableList<String>
}

data class RouteOffsetRuleNotesLengthRule(var maxLength: Int) : RouteOffsetRuleRule {
    override fun check(item: RouteOffsetRule): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RouteOffsetRuleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RouteOffsetRuleRule {
    override fun check(item: RouteOffsetRule): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RouteOffsetRuleTitleLengthRule(var maxLength: Int) : RouteOffsetRuleRule {
    override fun check(item: RouteOffsetRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a route offset rule. */
data class RouteOffsetRuleValidator(var rules: MutableList<RouteOffsetRuleRule>) {
    fun validate(item: RouteOffsetRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RouteOffsetRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RouteOffsetRule>): MutableMap<String, MutableList<String>> {
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

fun defaultRouteOffsetRuleValidator(): RouteOffsetRuleValidator {
    return RouteOffsetRuleValidator(
        mutableListOf<RouteOffsetRuleRule>(
            RouteOffsetRuleNotesLengthRule(80),
            RouteOffsetRuleTagsLimitRule(1000, mutableListOf<String>("global", "strict", "seasonal")),
            RouteOffsetRuleTitleLengthRule(64),
        ),
    )
}
