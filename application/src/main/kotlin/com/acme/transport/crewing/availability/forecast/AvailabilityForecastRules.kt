package com.acme.transport.crewing.availability.forecast

/** A rule a availability forecast must pass. Check returns its problems, or none. */
interface AvailabilityForecastRule {
    fun check(item: AvailabilityForecast): MutableList<String>
}

data class AvailabilityForecastRequiredNotesRule(var message: String) : AvailabilityForecastRule {
    override fun check(item: AvailabilityForecast): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityForecastNotesLengthRule(var maxLength: Int) : AvailabilityForecastRule {
    override fun check(item: AvailabilityForecast): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityForecastWeightRangeRule(var min: Double, var max: Double) : AvailabilityForecastRule {
    override fun check(item: AvailabilityForecast): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability forecast. */
data class AvailabilityForecastValidator(var rules: MutableList<AvailabilityForecastRule>) {
    fun validate(item: AvailabilityForecast): MutableList<String> {
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

    fun requireValid(item: AvailabilityForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityForecastValidator(): AvailabilityForecastValidator {
    return AvailabilityForecastValidator(
        mutableListOf<AvailabilityForecastRule>(
            AvailabilityForecastRequiredNotesRule("availability forecast needs this field"),
            AvailabilityForecastNotesLengthRule(1000),
            AvailabilityForecastWeightRangeRule(1.9, 2683.6),
        ),
    )
}
