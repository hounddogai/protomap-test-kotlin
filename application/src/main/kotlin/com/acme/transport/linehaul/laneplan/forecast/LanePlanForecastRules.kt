package com.acme.transport.linehaul.laneplan.forecast

/** A rule a lane plan forecast must pass. Check returns its problems, or none. */
interface LanePlanForecastRule {
    fun check(item: LanePlanForecast): MutableList<String>
}

data class LanePlanForecastRequiredCategoryRule(var message: String) : LanePlanForecastRule {
    override fun check(item: LanePlanForecast): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanForecastShiftCodeLengthRule(var maxLength: Int) : LanePlanForecastRule {
    override fun check(item: LanePlanForecast): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanForecastCreatedAtRangeRule(var min: Int, var max: Int) : LanePlanForecastRule {
    override fun check(item: LanePlanForecast): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanForecastRequiredShiftCodeRule(var message: String) : LanePlanForecastRule {
    override fun check(item: LanePlanForecast): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanForecastPriorityRangeRule(var min: Int, var max: Int) : LanePlanForecastRule {
    override fun check(item: LanePlanForecast): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanForecastCategoryLengthRule(var maxLength: Int) : LanePlanForecastRule {
    override fun check(item: LanePlanForecast): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan forecast. */
data class LanePlanForecastValidator(var rules: MutableList<LanePlanForecastRule>) {
    fun validate(item: LanePlanForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanForecastValidator(): LanePlanForecastValidator {
    return LanePlanForecastValidator(
        mutableListOf<LanePlanForecastRule>(
            LanePlanForecastRequiredCategoryRule("lane plan forecast needs this field"),
            LanePlanForecastShiftCodeLengthRule(120),
            LanePlanForecastCreatedAtRangeRule(16, 3600),
            LanePlanForecastRequiredShiftCodeRule("lane plan forecast needs this field"),
            LanePlanForecastPriorityRangeRule(16, 80),
            LanePlanForecastCategoryLengthRule(64),
        ),
    )
}
