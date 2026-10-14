package com.acme.transport.crewing.availability.reservation

/** A rule a scheduled availability forecast must pass. Check returns its problems, or none. */
interface ScheduledAvailabilityForecastRule {
    fun check(item: ScheduledAvailabilityForecast): MutableList<String>
}

data class ScheduledAvailabilityForecastCodeLengthRule(var maxLength: Int) : ScheduledAvailabilityForecastRule {
    override fun check(item: ScheduledAvailabilityForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityForecastThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledAvailabilityForecastRule {
    override fun check(item: ScheduledAvailabilityForecast): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class ScheduledAvailabilityForecastRevisionRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledAvailabilityForecastRule {
    override fun check(item: ScheduledAvailabilityForecast): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityForecastTitleLengthRule(
    var maxLength: Int,
) : ScheduledAvailabilityForecastRule {
    override fun check(item: ScheduledAvailabilityForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityForecastTimezoneLengthRule(
    var maxLength: Int,
) : ScheduledAvailabilityForecastRule {
    override fun check(item: ScheduledAvailabilityForecast): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled availability forecast. */
data class ScheduledAvailabilityForecastValidator(var rules: MutableList<ScheduledAvailabilityForecastRule>) {
    fun validate(item: ScheduledAvailabilityForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAvailabilityForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAvailabilityForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAvailabilityForecastValidator(): ScheduledAvailabilityForecastValidator {
    return ScheduledAvailabilityForecastValidator(
        mutableListOf<ScheduledAvailabilityForecastRule>(
            ScheduledAvailabilityForecastCodeLengthRule(1000),
            ScheduledAvailabilityForecastThresholdRangeRule(2.8, 4692.3),
            ScheduledAvailabilityForecastRevisionRangeRule(64, 255),
            ScheduledAvailabilityForecastTitleLengthRule(80),
            ScheduledAvailabilityForecastTimezoneLengthRule(3600),
        ),
    )
}
