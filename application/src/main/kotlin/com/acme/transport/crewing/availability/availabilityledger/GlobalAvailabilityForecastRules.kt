package com.acme.transport.crewing.availability.availabilityledger

/** A rule a global availability forecast must pass. Check returns its problems, or none. */
interface GlobalAvailabilityForecastRule {
    fun check(item: GlobalAvailabilityForecast): MutableList<String>
}

data class GlobalAvailabilityForecastRequiredCodeRule(var message: String) : GlobalAvailabilityForecastRule {
    override fun check(item: GlobalAvailabilityForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityForecastCapacityRangeRule(
    var min: Int,
    var max: Int,
) : GlobalAvailabilityForecastRule {
    override fun check(item: GlobalAvailabilityForecast): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityForecastCodeLengthRule(var maxLength: Int) : GlobalAvailabilityForecastRule {
    override fun check(item: GlobalAvailabilityForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global availability forecast. */
data class GlobalAvailabilityForecastValidator(var rules: MutableList<GlobalAvailabilityForecastRule>) {
    fun validate(item: GlobalAvailabilityForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalAvailabilityForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAvailabilityForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAvailabilityForecastValidator(): GlobalAvailabilityForecastValidator {
    return GlobalAvailabilityForecastValidator(
        mutableListOf<GlobalAvailabilityForecastRule>(
            GlobalAvailabilityForecastRequiredCodeRule("global availability forecast needs this field"),
            GlobalAvailabilityForecastCapacityRangeRule(255, 1000),
            GlobalAvailabilityForecastCodeLengthRule(86400),
        ),
    )
}
