package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

/** A rule a manual accessorial forecast must pass. Check returns its problems, or none. */
interface ManualAccessorialForecastRule {
    fun check(item: ManualAccessorialForecast): MutableList<String>
}

data class ManualAccessorialForecastTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ManualAccessorialForecastRule {
    override fun check(item: ManualAccessorialForecast): MutableList<String> {
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

data class ManualAccessorialForecastBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ManualAccessorialForecastRule {
    override fun check(item: ManualAccessorialForecast): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialForecastRequiredCodeRule(var message: String) : ManualAccessorialForecastRule {
    override fun check(item: ManualAccessorialForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual accessorial forecast. */
data class ManualAccessorialForecastValidator(var rules: MutableList<ManualAccessorialForecastRule>) {
    fun validate(item: ManualAccessorialForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAccessorialForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAccessorialForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAccessorialForecastValidator(): ManualAccessorialForecastValidator {
    return ManualAccessorialForecastValidator(
        mutableListOf<ManualAccessorialForecastRule>(
            ManualAccessorialForecastTagsLimitRule(32, mutableListOf<String>("regional")),
            ManualAccessorialForecastBudgetRangeRule(7.8, 1362.6),
            ManualAccessorialForecastRequiredCodeRule("manual accessorial forecast needs a value"),
        ),
    )
}
