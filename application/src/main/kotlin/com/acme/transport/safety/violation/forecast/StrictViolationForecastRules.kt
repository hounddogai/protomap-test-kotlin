package com.acme.transport.safety.violation.forecast

/** A rule a strict violation forecast must pass. Check returns its problems, or none. */
interface StrictViolationForecastRule {
    fun check(item: StrictViolationForecast): MutableList<String>
}

data class StrictViolationForecastContactEmailLengthRule(var maxLength: Int) : StrictViolationForecastRule {
    override fun check(item: StrictViolationForecast): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationForecastRequiredContactEmailRule(var message: String) : StrictViolationForecastRule {
    override fun check(item: StrictViolationForecast): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationForecastRetriesRangeRule(var min: Int, var max: Int) : StrictViolationForecastRule {
    override fun check(item: StrictViolationForecast): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class StrictViolationForecastRequiredTitleRule(var message: String) : StrictViolationForecastRule {
    override fun check(item: StrictViolationForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationForecastCodeLengthRule(var maxLength: Int) : StrictViolationForecastRule {
    override fun check(item: StrictViolationForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictViolationForecastTitleLengthRule(var maxLength: Int) : StrictViolationForecastRule {
    override fun check(item: StrictViolationForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict violation forecast. */
data class StrictViolationForecastValidator(var rules: MutableList<StrictViolationForecastRule>) {
    fun validate(item: StrictViolationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictViolationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictViolationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictViolationForecastValidator(): StrictViolationForecastValidator {
    return StrictViolationForecastValidator(
        mutableListOf<StrictViolationForecastRule>(
            StrictViolationForecastContactEmailLengthRule(1000),
            StrictViolationForecastRequiredContactEmailRule("strict violation forecast needs an entry"),
            StrictViolationForecastRetriesRangeRule(80, 255),
            StrictViolationForecastRequiredTitleRule("strict violation forecast needs a value"),
            StrictViolationForecastCodeLengthRule(16),
            StrictViolationForecastTitleLengthRule(80),
        ),
    )
}
