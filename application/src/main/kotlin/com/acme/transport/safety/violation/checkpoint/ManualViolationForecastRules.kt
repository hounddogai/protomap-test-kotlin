package com.acme.transport.safety.violation.checkpoint

/** A rule a manual violation forecast must pass. Check returns its problems, or none. */
interface ManualViolationForecastRule {
    fun check(item: ManualViolationForecast): MutableList<String>
}

data class ManualViolationForecastRevisionRangeRule(var min: Int, var max: Int) : ManualViolationForecastRule {
    override fun check(item: ManualViolationForecast): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class ManualViolationForecastCodePrefixRule(var prefix: String) : ManualViolationForecastRule {
    override fun check(item: ManualViolationForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationForecastRequiredLabelRule(var message: String) : ManualViolationForecastRule {
    override fun check(item: ManualViolationForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationForecastEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ManualViolationForecastRule {
    override fun check(item: ManualViolationForecast): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualViolationForecastCeilingRangeRule(
    var min: Double,
    var max: Double,
) : ManualViolationForecastRule {
    override fun check(item: ManualViolationForecast): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class ManualViolationForecastLabelLengthRule(var maxLength: Int) : ManualViolationForecastRule {
    override fun check(item: ManualViolationForecast): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual violation forecast. */
data class ManualViolationForecastValidator(var rules: MutableList<ManualViolationForecastRule>) {
    fun validate(item: ManualViolationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualViolationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualViolationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualViolationForecastValidator(): ManualViolationForecastValidator {
    return ManualViolationForecastValidator(
        mutableListOf<ManualViolationForecastRule>(
            ManualViolationForecastRevisionRangeRule(64, 1000),
            ManualViolationForecastCodePrefixRule("REF"),
            ManualViolationForecastRequiredLabelRule("manual violation forecast needs an entry"),
            ManualViolationForecastEtaMinutesRangeRule(64, 64),
            ManualViolationForecastCeilingRangeRule(9.1, 4018.0),
            ManualViolationForecastLabelLengthRule(16),
        ),
    )
}
