package com.acme.transport.crewing.dutyperiod.milestone

/** A rule a weighted duty period exception must pass. Check returns its problems, or none. */
interface WeightedDutyPeriodExceptionRule {
    fun check(item: WeightedDutyPeriodException): MutableList<String>
}

data class WeightedDutyPeriodExceptionRateRangeRule(
    var min: Double,
    var max: Double,
) : WeightedDutyPeriodExceptionRule {
    override fun check(item: WeightedDutyPeriodException): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodExceptionCapacityRangeRule(
    var min: Int,
    var max: Int,
) : WeightedDutyPeriodExceptionRule {
    override fun check(item: WeightedDutyPeriodException): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodExceptionRequiredSourceRule(
    var message: String,
) : WeightedDutyPeriodExceptionRule {
    override fun check(item: WeightedDutyPeriodException): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodExceptionRequiredContactEmailRule(
    var message: String,
) : WeightedDutyPeriodExceptionRule {
    override fun check(item: WeightedDutyPeriodException): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodExceptionRequiredNameRule(var message: String) : WeightedDutyPeriodExceptionRule {
    override fun check(item: WeightedDutyPeriodException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodExceptionRequiredCodeRule(var message: String) : WeightedDutyPeriodExceptionRule {
    override fun check(item: WeightedDutyPeriodException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted duty period exception. */
data class WeightedDutyPeriodExceptionValidator(var rules: MutableList<WeightedDutyPeriodExceptionRule>) {
    fun validate(item: WeightedDutyPeriodException): MutableList<String> {
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

    fun requireValid(item: WeightedDutyPeriodException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDutyPeriodException>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDutyPeriodExceptionValidator(): WeightedDutyPeriodExceptionValidator {
    return WeightedDutyPeriodExceptionValidator(
        mutableListOf<WeightedDutyPeriodExceptionRule>(
            WeightedDutyPeriodExceptionRateRangeRule(6.9, 2907.9),
            WeightedDutyPeriodExceptionCapacityRangeRule(64, 3600),
            WeightedDutyPeriodExceptionRequiredSourceRule("weighted duty period exception needs an entry"),
            WeightedDutyPeriodExceptionRequiredContactEmailRule("weighted duty period exception needs a value"),
            WeightedDutyPeriodExceptionRequiredNameRule("weighted duty period exception needs this field"),
            WeightedDutyPeriodExceptionRequiredCodeRule("weighted duty period exception needs this field"),
        ),
    )
}
