package com.acme.transport.customs.clearance.policy

/** A rule a clearance forecast must pass. Check returns its problems, or none. */
interface ClearanceForecastRule {
    fun check(item: ClearanceForecast): MutableList<String>
}

data class ClearanceForecastRequiredCodeRule(var message: String) : ClearanceForecastRule {
    override fun check(item: ClearanceForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceForecastPriorityRangeRule(var min: Int, var max: Int) : ClearanceForecastRule {
    override fun check(item: ClearanceForecast): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class ClearanceForecastCodeLengthRule(var maxLength: Int) : ClearanceForecastRule {
    override fun check(item: ClearanceForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceForecastRequiredTitleRule(var message: String) : ClearanceForecastRule {
    override fun check(item: ClearanceForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceForecastTitleLengthRule(var maxLength: Int) : ClearanceForecastRule {
    override fun check(item: ClearanceForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceForecastCodePrefixRule(var prefix: String) : ClearanceForecastRule {
    override fun check(item: ClearanceForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance forecast. */
data class ClearanceForecastValidator(var rules: MutableList<ClearanceForecastRule>) {
    fun validate(item: ClearanceForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceForecastValidator(): ClearanceForecastValidator {
    return ClearanceForecastValidator(
        mutableListOf<ClearanceForecastRule>(
            ClearanceForecastRequiredCodeRule("clearance forecast needs a value"),
            ClearanceForecastPriorityRangeRule(64, 3600),
            ClearanceForecastCodeLengthRule(64),
            ClearanceForecastRequiredTitleRule("clearance forecast needs a value"),
            ClearanceForecastTitleLengthRule(32),
            ClearanceForecastCodePrefixRule("ACM"),
        ),
    )
}
