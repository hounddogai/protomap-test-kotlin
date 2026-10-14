package com.acme.transport.customs.tariff.tariffschedule

/** A rule a fallback tariff schedule must pass. Check returns its problems, or none. */
interface FallbackTariffScheduleRule {
    fun check(item: FallbackTariffSchedule): MutableList<String>
}

data class FallbackTariffScheduleLimitRangeRule(var min: Int, var max: Int) : FallbackTariffScheduleRule {
    override fun check(item: FallbackTariffSchedule): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class FallbackTariffScheduleCodeLengthRule(var maxLength: Int) : FallbackTariffScheduleRule {
    override fun check(item: FallbackTariffSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTariffScheduleTrailerIdLengthRule(var maxLength: Int) : FallbackTariffScheduleRule {
    override fun check(item: FallbackTariffSchedule): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tariff schedule. */
data class FallbackTariffScheduleValidator(var rules: MutableList<FallbackTariffScheduleRule>) {
    fun validate(item: FallbackTariffSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTariffSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTariffSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTariffScheduleValidator(): FallbackTariffScheduleValidator {
    return FallbackTariffScheduleValidator(
        mutableListOf<FallbackTariffScheduleRule>(
            FallbackTariffScheduleLimitRangeRule(255, 3600),
            FallbackTariffScheduleCodeLengthRule(1000),
            FallbackTariffScheduleTrailerIdLengthRule(1000),
        ),
    )
}
