package com.acme.transport.lastmile.locker.forecast

/** A rule a locker forecast must pass. Check returns its problems, or none. */
interface LockerForecastRule {
    fun check(item: LockerForecast): MutableList<String>
}

data class LockerForecastRequiredCodeRule(var message: String) : LockerForecastRule {
    override fun check(item: LockerForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerForecastFloorValueRangeRule(var min: Double, var max: Double) : LockerForecastRule {
    override fun check(item: LockerForecast): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class LockerForecastPriorityRangeRule(var min: Int, var max: Int) : LockerForecastRule {
    override fun check(item: LockerForecast): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerForecastDockLengthRule(var maxLength: Int) : LockerForecastRule {
    override fun check(item: LockerForecast): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerForecastCodeLengthRule(var maxLength: Int) : LockerForecastRule {
    override fun check(item: LockerForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerForecastPinnedGuardRule(var limit: Double) : LockerForecastRule {
    override fun check(item: LockerForecast): MutableList<String> {
        if (item.pinned && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker forecast. */
data class LockerForecastValidator(var rules: MutableList<LockerForecastRule>) {
    fun validate(item: LockerForecast): MutableList<String> {
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

    fun requireValid(item: LockerForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerForecastValidator(): LockerForecastValidator {
    return LockerForecastValidator(
        mutableListOf<LockerForecastRule>(
            LockerForecastRequiredCodeRule("locker forecast needs a value"),
            LockerForecastFloorValueRangeRule(8.0, 2166.4),
            LockerForecastPriorityRangeRule(16, 32),
            LockerForecastDockLengthRule(1000),
            LockerForecastCodeLengthRule(120),
            LockerForecastPinnedGuardRule(3.0),
        ),
    )
}
