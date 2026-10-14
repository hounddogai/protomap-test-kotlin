package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.text.normalize

/** A rule a coaching batch must pass. Check returns its problems, or none. */
interface CoachingBatchRule {
    fun check(item: CoachingBatch): MutableList<String>
}

data class CoachingBatchCodePrefixRule(var prefix: String) : CoachingBatchRule {
    override fun check(item: CoachingBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingBatchOdometerRangeRule(var min: Int, var max: Int) : CoachingBatchRule {
    override fun check(item: CoachingBatch): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class CoachingBatchAllowedNameRule(var allowed: MutableList<String>) : CoachingBatchRule {
    override fun check(item: CoachingBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching batch. */
data class CoachingBatchValidator(var rules: MutableList<CoachingBatchRule>) {
    fun validate(item: CoachingBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CoachingBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingBatchValidator(): CoachingBatchValidator {
    return CoachingBatchValidator(
        mutableListOf<CoachingBatchRule>(
            CoachingBatchCodePrefixRule("X"),
            CoachingBatchOdometerRangeRule(32, 80),
            CoachingBatchAllowedNameRule(mutableListOf<String>("legacy", "tiered", "regional")),
        ),
    )
}
