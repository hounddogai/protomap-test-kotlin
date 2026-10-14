package com.acme.transport.network.cutoff.forecast

/** A rule a default cutoff forecast must pass. Check returns its problems, or none. */
interface DefaultCutoffForecastRule {
    fun check(item: DefaultCutoffForecast): MutableList<String>
}

data class DefaultCutoffForecastTitleLengthRule(var maxLength: Int) : DefaultCutoffForecastRule {
    override fun check(item: DefaultCutoffForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffForecastRequiredTitleRule(var message: String) : DefaultCutoffForecastRule {
    override fun check(item: DefaultCutoffForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCutoffForecastCodeLengthRule(var maxLength: Int) : DefaultCutoffForecastRule {
    override fun check(item: DefaultCutoffForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default cutoff forecast. */
data class DefaultCutoffForecastValidator(var rules: MutableList<DefaultCutoffForecastRule>) {
    fun validate(item: DefaultCutoffForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCutoffForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCutoffForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCutoffForecastValidator(): DefaultCutoffForecastValidator {
    return DefaultCutoffForecastValidator(
        mutableListOf<DefaultCutoffForecastRule>(
            DefaultCutoffForecastTitleLengthRule(86400),
            DefaultCutoffForecastRequiredTitleRule("default cutoff forecast needs an entry"),
            DefaultCutoffForecastCodeLengthRule(86400),
        ),
    )
}
