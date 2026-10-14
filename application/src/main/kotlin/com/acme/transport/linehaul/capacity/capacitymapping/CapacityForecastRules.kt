package com.acme.transport.linehaul.capacity.capacitymapping

/** A rule a capacity forecast must pass. Check returns its problems, or none. */
interface CapacityForecastRule {
    fun check(item: CapacityForecast): MutableList<String>
}

data class CapacityForecastRequiredCodeRule(var message: String) : CapacityForecastRule {
    override fun check(item: CapacityForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityForecastStopCountRangeRule(var min: Int, var max: Int) : CapacityForecastRule {
    override fun check(item: CapacityForecast): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class CapacityForecastRequiredCategoryRule(var message: String) : CapacityForecastRule {
    override fun check(item: CapacityForecast): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityForecastNameLengthRule(var maxLength: Int) : CapacityForecastRule {
    override fun check(item: CapacityForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityForecastRequiredNameRule(var message: String) : CapacityForecastRule {
    override fun check(item: CapacityForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity forecast. */
data class CapacityForecastValidator(var rules: MutableList<CapacityForecastRule>) {
    fun validate(item: CapacityForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityForecastValidator(): CapacityForecastValidator {
    return CapacityForecastValidator(
        mutableListOf<CapacityForecastRule>(
            CapacityForecastRequiredCodeRule("capacity forecast needs this field"),
            CapacityForecastStopCountRangeRule(64, 1000),
            CapacityForecastRequiredCategoryRule("capacity forecast needs a value"),
            CapacityForecastNameLengthRule(120),
            CapacityForecastRequiredNameRule("capacity forecast needs an entry"),
        ),
    )
}
