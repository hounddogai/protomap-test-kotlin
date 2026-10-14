package com.acme.transport.linehaul.relay.relaythreshold

/** A rule a relay forecast must pass. Check returns its problems, or none. */
interface RelayForecastRule {
    fun check(item: RelayForecast): MutableList<String>
}

data class RelayForecastLaneLengthRule(var maxLength: Int) : RelayForecastRule {
    override fun check(item: RelayForecast): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayForecastRequiredTimezoneRule(var message: String) : RelayForecastRule {
    override fun check(item: RelayForecast): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayForecastTimezoneLengthRule(var maxLength: Int) : RelayForecastRule {
    override fun check(item: RelayForecast): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayForecastRequiredLaneRule(var message: String) : RelayForecastRule {
    override fun check(item: RelayForecast): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayForecastCodeLengthRule(var maxLength: Int) : RelayForecastRule {
    override fun check(item: RelayForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay forecast. */
data class RelayForecastValidator(var rules: MutableList<RelayForecastRule>) {
    fun validate(item: RelayForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayForecastValidator(): RelayForecastValidator {
    return RelayForecastValidator(
        mutableListOf<RelayForecastRule>(
            RelayForecastLaneLengthRule(86400),
            RelayForecastRequiredTimezoneRule("relay forecast needs an entry"),
            RelayForecastTimezoneLengthRule(86400),
            RelayForecastRequiredLaneRule("relay forecast needs this field"),
            RelayForecastCodeLengthRule(64),
        ),
    )
}
