package com.acme.transport.lastmile.drop.reservation

/** A rule a drop forecast must pass. Check returns its problems, or none. */
interface DropForecastRule {
    fun check(item: DropForecast): MutableList<String>
}

data class DropForecastCodeLengthRule(var maxLength: Int) : DropForecastRule {
    override fun check(item: DropForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropForecastRequiredCodeRule(var message: String) : DropForecastRule {
    override fun check(item: DropForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropForecastRequiredTitleRule(var message: String) : DropForecastRule {
    override fun check(item: DropForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop forecast. */
data class DropForecastValidator(var rules: MutableList<DropForecastRule>) {
    fun validate(item: DropForecast): MutableList<String> {
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

    fun requireValid(item: DropForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDropForecastValidator(): DropForecastValidator {
    return DropForecastValidator(
        mutableListOf<DropForecastRule>(
            DropForecastCodeLengthRule(64),
            DropForecastRequiredCodeRule("drop forecast needs an entry"),
            DropForecastRequiredTitleRule("drop forecast needs a value"),
        ),
    )
}
