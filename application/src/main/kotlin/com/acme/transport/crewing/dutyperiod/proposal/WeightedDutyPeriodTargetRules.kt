package com.acme.transport.crewing.dutyperiod.proposal

import com.acme.transport.kit.text.normalize

/** A rule a weighted duty period target must pass. Check returns its problems, or none. */
interface WeightedDutyPeriodTargetRule {
    fun check(item: WeightedDutyPeriodTarget): MutableList<String>
}

data class WeightedDutyPeriodTargetRequiredCodeRule(var message: String) : WeightedDutyPeriodTargetRule {
    override fun check(item: WeightedDutyPeriodTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodTargetLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : WeightedDutyPeriodTargetRule {
    override fun check(item: WeightedDutyPeriodTarget): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodTargetAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedDutyPeriodTargetRule {
    override fun check(item: WeightedDutyPeriodTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted duty period target. */
data class WeightedDutyPeriodTargetValidator(var rules: MutableList<WeightedDutyPeriodTargetRule>) {
    fun validate(item: WeightedDutyPeriodTarget): MutableList<String> {
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

    fun requireValid(item: WeightedDutyPeriodTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDutyPeriodTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDutyPeriodTargetValidator(): WeightedDutyPeriodTargetValidator {
    return WeightedDutyPeriodTargetValidator(
        mutableListOf<WeightedDutyPeriodTargetRule>(
            WeightedDutyPeriodTargetRequiredCodeRule("weighted duty period target needs a value"),
            WeightedDutyPeriodTargetLoadKgRangeRule(9.7, 2792.8),
            WeightedDutyPeriodTargetAllowedNameRule(mutableListOf<String>("batched", "default")),
        ),
    )
}
