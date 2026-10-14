package com.acme.transport.emissions.idling.forecast

/** A rule a weighted idling target must pass. Check returns its problems, or none. */
interface WeightedIdlingTargetRule {
    fun check(item: WeightedIdlingTarget): MutableList<String>
}

data class WeightedIdlingTargetAttemptsRangeRule(var min: Int, var max: Int) : WeightedIdlingTargetRule {
    override fun check(item: WeightedIdlingTarget): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedIdlingTargetTitleLengthRule(var maxLength: Int) : WeightedIdlingTargetRule {
    override fun check(item: WeightedIdlingTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedIdlingTargetLaneLengthRule(var maxLength: Int) : WeightedIdlingTargetRule {
    override fun check(item: WeightedIdlingTarget): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted idling target. */
data class WeightedIdlingTargetValidator(var rules: MutableList<WeightedIdlingTargetRule>) {
    fun validate(item: WeightedIdlingTarget): MutableList<String> {
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

    fun requireValid(item: WeightedIdlingTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedIdlingTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedIdlingTargetValidator(): WeightedIdlingTargetValidator {
    return WeightedIdlingTargetValidator(
        mutableListOf<WeightedIdlingTargetRule>(
            WeightedIdlingTargetAttemptsRangeRule(16, 64),
            WeightedIdlingTargetTitleLengthRule(120),
            WeightedIdlingTargetLaneLengthRule(16),
        ),
    )
}
