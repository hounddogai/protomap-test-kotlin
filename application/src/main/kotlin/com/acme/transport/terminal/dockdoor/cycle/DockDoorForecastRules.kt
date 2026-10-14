package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.normalize

/** A rule a dock door forecast must pass. Check returns its problems, or none. */
interface DockDoorForecastRule {
    fun check(item: DockDoorForecast): MutableList<String>
}

data class DockDoorForecastAllowedCodeRule(var allowed: MutableList<String>) : DockDoorForecastRule {
    override fun check(item: DockDoorForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorForecastNameLengthRule(var maxLength: Int) : DockDoorForecastRule {
    override fun check(item: DockDoorForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorForecastCodeLengthRule(var maxLength: Int) : DockDoorForecastRule {
    override fun check(item: DockDoorForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorForecastMaximumRangeRule(var min: Double, var max: Double) : DockDoorForecastRule {
    override fun check(item: DockDoorForecast): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class DockDoorForecastBudgetRangeRule(var min: Double, var max: Double) : DockDoorForecastRule {
    override fun check(item: DockDoorForecast): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door forecast. */
data class DockDoorForecastValidator(var rules: MutableList<DockDoorForecastRule>) {
    fun validate(item: DockDoorForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DockDoorForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorForecastValidator(): DockDoorForecastValidator {
    return DockDoorForecastValidator(
        mutableListOf<DockDoorForecastRule>(
            DockDoorForecastAllowedCodeRule(mutableListOf<String>("manual", "seasonal")),
            DockDoorForecastNameLengthRule(1000),
            DockDoorForecastCodeLengthRule(120),
            DockDoorForecastMaximumRangeRule(1.4, 1253.6),
            DockDoorForecastBudgetRangeRule(8.9, 2668.6),
        ),
    )
}
