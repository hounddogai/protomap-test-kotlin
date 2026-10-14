package com.acme.transport.customs.inspection.draft

/** A rule a inspection forecast must pass. Check returns its problems, or none. */
interface InspectionForecastRule {
    fun check(item: InspectionForecast): MutableList<String>
}

data class InspectionForecastRequiredCodeRule(var message: String) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastSummaryLengthRule(var maxLength: Int) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastRequiredSummaryRule(var message: String) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastUnitLengthRule(var maxLength: Int) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastCostRangeRule(var min: Double, var max: Double) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection forecast. */
data class InspectionForecastValidator(var rules: MutableList<InspectionForecastRule>) {
    fun validate(item: InspectionForecast): MutableList<String> {
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

    fun requireValid(item: InspectionForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionForecastValidator(): InspectionForecastValidator {
    return InspectionForecastValidator(
        mutableListOf<InspectionForecastRule>(
            InspectionForecastRequiredCodeRule("inspection forecast needs this field"),
            InspectionForecastSummaryLengthRule(86400),
            InspectionForecastRequiredSummaryRule("inspection forecast needs an entry"),
            InspectionForecastUnitLengthRule(3600),
            InspectionForecastCostRangeRule(9.1, 3113.4),
        ),
    )
}
