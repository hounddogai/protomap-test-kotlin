package com.acme.transport.crewing.dutyperiod.slot

import com.acme.transport.kit.text.normalize

/** A rule a duty period forecast must pass. Check returns its problems, or none. */
interface DutyPeriodForecastRule {
    fun check(item: DutyPeriodForecast): MutableList<String>
}

data class DutyPeriodForecastNameLengthRule(var maxLength: Int) : DutyPeriodForecastRule {
    override fun check(item: DutyPeriodForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodForecastOwnerIdLengthRule(var maxLength: Int) : DutyPeriodForecastRule {
    override fun check(item: DutyPeriodForecast): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodForecastRequiredOwnerIdRule(var message: String) : DutyPeriodForecastRule {
    override fun check(item: DutyPeriodForecast): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodForecastRatioRangeRule(var min: Double, var max: Double) : DutyPeriodForecastRule {
    override fun check(item: DutyPeriodForecast): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodForecastAllowedNameRule(var allowed: MutableList<String>) : DutyPeriodForecastRule {
    override fun check(item: DutyPeriodForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodForecastRequiredCodeRule(var message: String) : DutyPeriodForecastRule {
    override fun check(item: DutyPeriodForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period forecast. */
data class DutyPeriodForecastValidator(var rules: MutableList<DutyPeriodForecastRule>) {
    fun validate(item: DutyPeriodForecast): MutableList<String> {
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

    fun requireValid(item: DutyPeriodForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodForecastValidator(): DutyPeriodForecastValidator {
    return DutyPeriodForecastValidator(
        mutableListOf<DutyPeriodForecastRule>(
            DutyPeriodForecastNameLengthRule(86400),
            DutyPeriodForecastOwnerIdLengthRule(80),
            DutyPeriodForecastRequiredOwnerIdRule("duty period forecast needs an entry"),
            DutyPeriodForecastRatioRangeRule(5.5, 1044.4),
            DutyPeriodForecastAllowedNameRule(mutableListOf<String>("capped", "primary", "seasonal")),
            DutyPeriodForecastRequiredCodeRule("duty period forecast needs this field"),
        ),
    )
}
