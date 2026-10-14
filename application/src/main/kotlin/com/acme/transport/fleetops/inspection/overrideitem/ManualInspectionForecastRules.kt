package com.acme.transport.fleetops.inspection.overrideitem

/** A rule a manual inspection forecast must pass. Check returns its problems, or none. */
interface ManualInspectionForecastRule {
    fun check(item: ManualInspectionForecast): MutableList<String>
}

data class ManualInspectionForecastRequiredDescriptionRule(var message: String) : ManualInspectionForecastRule {
    override fun check(item: ManualInspectionForecast): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionForecastUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : ManualInspectionForecastRule {
    override fun check(item: ManualInspectionForecast): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionForecastRequiredTitleRule(var message: String) : ManualInspectionForecastRule {
    override fun check(item: ManualInspectionForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual inspection forecast. */
data class ManualInspectionForecastValidator(var rules: MutableList<ManualInspectionForecastRule>) {
    fun validate(item: ManualInspectionForecast): MutableList<String> {
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

    fun requireValid(item: ManualInspectionForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualInspectionForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualInspectionForecastValidator(): ManualInspectionForecastValidator {
    return ManualInspectionForecastValidator(
        mutableListOf<ManualInspectionForecastRule>(
            ManualInspectionForecastRequiredDescriptionRule("manual inspection forecast needs this field"),
            ManualInspectionForecastUpdatedAtRangeRule(255, 3600),
            ManualInspectionForecastRequiredTitleRule("manual inspection forecast needs this field"),
        ),
    )
}
