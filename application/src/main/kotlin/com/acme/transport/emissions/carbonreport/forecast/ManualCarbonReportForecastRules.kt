package com.acme.transport.emissions.carbonreport.forecast

/** A rule a manual carbon report forecast must pass. Check returns its problems, or none. */
interface ManualCarbonReportForecastRule {
    fun check(item: ManualCarbonReportForecast): MutableList<String>
}

data class ManualCarbonReportForecastCodeLengthRule(var maxLength: Int) : ManualCarbonReportForecastRule {
    override fun check(item: ManualCarbonReportForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportForecastPalletsRangeRule(
    var min: Int,
    var max: Int,
) : ManualCarbonReportForecastRule {
    override fun check(item: ManualCarbonReportForecast): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class ManualCarbonReportForecastRequiredCodeRule(var message: String) : ManualCarbonReportForecastRule {
    override fun check(item: ManualCarbonReportForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualCarbonReportForecastCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualCarbonReportForecastRule {
    override fun check(item: ManualCarbonReportForecast): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual carbon report forecast. */
data class ManualCarbonReportForecastValidator(var rules: MutableList<ManualCarbonReportForecastRule>) {
    fun validate(item: ManualCarbonReportForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualCarbonReportForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualCarbonReportForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualCarbonReportForecastValidator(): ManualCarbonReportForecastValidator {
    return ManualCarbonReportForecastValidator(
        mutableListOf<ManualCarbonReportForecastRule>(
            ManualCarbonReportForecastCodeLengthRule(120),
            ManualCarbonReportForecastPalletsRangeRule(32, 255),
            ManualCarbonReportForecastRequiredCodeRule("manual carbon report forecast needs an entry"),
            ManualCarbonReportForecastCreatedAtRangeRule(64, 255),
        ),
    )
}
