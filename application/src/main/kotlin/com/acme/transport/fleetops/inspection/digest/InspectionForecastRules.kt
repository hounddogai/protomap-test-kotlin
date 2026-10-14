package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.text.normalize

/** A rule a inspection forecast must pass. Check returns its problems, or none. */
interface InspectionForecastRule {
    fun check(item: InspectionForecast): MutableList<String>
}

data class InspectionForecastNameLengthRule(var maxLength: Int) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastAllowedNameRule(var allowed: MutableList<String>) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastRequiredCodeRule(var message: String) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastRequiredNameRule(var message: String) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionForecastCostRangeRule(var min: Double, var max: Double) : InspectionForecastRule {
    override fun check(item: InspectionForecast): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection forecast. */
data class InspectionForecastValidator(var rules: MutableList<InspectionForecastRule>) {
    fun validate(item: InspectionForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
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
            InspectionForecastNameLengthRule(64),
            InspectionForecastAllowedNameRule(mutableListOf<String>()),
            InspectionForecastRequiredCodeRule("inspection forecast needs this field"),
            InspectionForecastRequiredNameRule("inspection forecast needs an entry"),
            InspectionForecastCostRangeRule(9.7, 1056.8),
        ),
    )
}
