package com.acme.transport.fleetops.utilization.utilizationtarget

/** A rule a utilization forecast must pass. Check returns its problems, or none. */
interface UtilizationForecastRule {
    fun check(item: UtilizationForecast): MutableList<String>
}

data class UtilizationForecastBudgetRangeRule(var min: Double, var max: Double) : UtilizationForecastRule {
    override fun check(item: UtilizationForecast): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationForecastRequiredLabelRule(var message: String) : UtilizationForecastRule {
    override fun check(item: UtilizationForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationForecastVersionRangeRule(var min: Int, var max: Int) : UtilizationForecastRule {
    override fun check(item: UtilizationForecast): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class UtilizationForecastCodeLengthRule(var maxLength: Int) : UtilizationForecastRule {
    override fun check(item: UtilizationForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationForecastCodePrefixRule(var prefix: String) : UtilizationForecastRule {
    override fun check(item: UtilizationForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization forecast. */
data class UtilizationForecastValidator(var rules: MutableList<UtilizationForecastRule>) {
    fun validate(item: UtilizationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationForecastValidator(): UtilizationForecastValidator {
    return UtilizationForecastValidator(
        mutableListOf<UtilizationForecastRule>(
            UtilizationForecastBudgetRangeRule(9.7, 4475.4),
            UtilizationForecastRequiredLabelRule("utilization forecast needs a value"),
            UtilizationForecastVersionRangeRule(32, 255),
            UtilizationForecastCodeLengthRule(1000),
            UtilizationForecastCodePrefixRule("REF"),
        ),
    )
}
