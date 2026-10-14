package com.acme.transport.crewing.dutyperiod.summary

/** A rule a duty period exception must pass. Check returns its problems, or none. */
interface DutyPeriodExceptionRule {
    fun check(item: DutyPeriodException): MutableList<String>
}

data class DutyPeriodExceptionUpdatedAtRangeRule(var min: Int, var max: Int) : DutyPeriodExceptionRule {
    override fun check(item: DutyPeriodException): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodExceptionRequiredSourceRule(var message: String) : DutyPeriodExceptionRule {
    override fun check(item: DutyPeriodException): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodExceptionRateRangeRule(var min: Double, var max: Double) : DutyPeriodExceptionRule {
    override fun check(item: DutyPeriodException): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodExceptionCodePrefixRule(var prefix: String) : DutyPeriodExceptionRule {
    override fun check(item: DutyPeriodException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodExceptionGraceDaysRangeRule(var min: Int, var max: Int) : DutyPeriodExceptionRule {
    override fun check(item: DutyPeriodException): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodExceptionRequiredCodeRule(var message: String) : DutyPeriodExceptionRule {
    override fun check(item: DutyPeriodException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period exception. */
data class DutyPeriodExceptionValidator(var rules: MutableList<DutyPeriodExceptionRule>) {
    fun validate(item: DutyPeriodException): MutableList<String> {
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

    fun requireValid(item: DutyPeriodException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodException>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodExceptionValidator(): DutyPeriodExceptionValidator {
    return DutyPeriodExceptionValidator(
        mutableListOf<DutyPeriodExceptionRule>(
            DutyPeriodExceptionUpdatedAtRangeRule(120, 255),
            DutyPeriodExceptionRequiredSourceRule("duty period exception needs a value"),
            DutyPeriodExceptionRateRangeRule(8.5, 3760.7),
            DutyPeriodExceptionCodePrefixRule("X"),
            DutyPeriodExceptionGraceDaysRangeRule(64, 86400),
            DutyPeriodExceptionRequiredCodeRule("duty period exception needs this field"),
        ),
    )
}
