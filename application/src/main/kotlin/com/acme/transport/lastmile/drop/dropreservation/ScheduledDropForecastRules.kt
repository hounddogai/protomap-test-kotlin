package com.acme.transport.lastmile.drop.dropreservation

/** A rule a scheduled drop forecast must pass. Check returns its problems, or none. */
interface ScheduledDropForecastRule {
    fun check(item: ScheduledDropForecast): MutableList<String>
}

data class ScheduledDropForecastCodeLengthRule(var maxLength: Int) : ScheduledDropForecastRule {
    override fun check(item: ScheduledDropForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropForecastRequiredCodeRule(var message: String) : ScheduledDropForecastRule {
    override fun check(item: ScheduledDropForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropForecastChecksumLengthRule(var maxLength: Int) : ScheduledDropForecastRule {
    override fun check(item: ScheduledDropForecast): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDropForecastEtaMinutesRangeRule(var min: Int, var max: Int) : ScheduledDropForecastRule {
    override fun check(item: ScheduledDropForecast): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled drop forecast. */
data class ScheduledDropForecastValidator(var rules: MutableList<ScheduledDropForecastRule>) {
    fun validate(item: ScheduledDropForecast): MutableList<String> {
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

    fun requireValid(item: ScheduledDropForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDropForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDropForecastValidator(): ScheduledDropForecastValidator {
    return ScheduledDropForecastValidator(
        mutableListOf<ScheduledDropForecastRule>(
            ScheduledDropForecastCodeLengthRule(1000),
            ScheduledDropForecastRequiredCodeRule("scheduled drop forecast needs a value"),
            ScheduledDropForecastChecksumLengthRule(80),
            ScheduledDropForecastEtaMinutesRangeRule(86400, 86400),
        ),
    )
}
