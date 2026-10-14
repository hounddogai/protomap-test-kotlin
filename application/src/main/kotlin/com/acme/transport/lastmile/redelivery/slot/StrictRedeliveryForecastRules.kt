package com.acme.transport.lastmile.redelivery.slot

/** A rule a strict redelivery forecast must pass. Check returns its problems, or none. */
interface StrictRedeliveryForecastRule {
    fun check(item: StrictRedeliveryForecast): MutableList<String>
}

data class StrictRedeliveryForecastRouteCodeLengthRule(var maxLength: Int) : StrictRedeliveryForecastRule {
    override fun check(item: StrictRedeliveryForecast): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryForecastLabelLengthRule(var maxLength: Int) : StrictRedeliveryForecastRule {
    override fun check(item: StrictRedeliveryForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryForecastRequiredRouteCodeRule(var message: String) : StrictRedeliveryForecastRule {
    override fun check(item: StrictRedeliveryForecast): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryForecastTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : StrictRedeliveryForecastRule {
    override fun check(item: StrictRedeliveryForecast): MutableList<String> {
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

/** Runs every rule of a strict redelivery forecast. */
data class StrictRedeliveryForecastValidator(var rules: MutableList<StrictRedeliveryForecastRule>) {
    fun validate(item: StrictRedeliveryForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRedeliveryForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRedeliveryForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRedeliveryForecastValidator(): StrictRedeliveryForecastValidator {
    return StrictRedeliveryForecastValidator(
        mutableListOf<StrictRedeliveryForecastRule>(
            StrictRedeliveryForecastRouteCodeLengthRule(80),
            StrictRedeliveryForecastLabelLengthRule(80),
            StrictRedeliveryForecastRequiredRouteCodeRule("strict redelivery forecast needs this field"),
            StrictRedeliveryForecastTagsLimitRule(255, mutableListOf<String>("seasonal", "strict")),
        ),
    )
}
