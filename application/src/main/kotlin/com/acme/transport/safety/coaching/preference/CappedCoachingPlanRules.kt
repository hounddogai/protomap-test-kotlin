package com.acme.transport.safety.coaching.preference

import com.acme.transport.kit.text.normalize

/** A rule a capped coaching plan must pass. Check returns its problems, or none. */
interface CappedCoachingPlanRule {
    fun check(item: CappedCoachingPlan): MutableList<String>
}

data class CappedCoachingPlanBatchSizeRangeRule(var min: Int, var max: Int) : CappedCoachingPlanRule {
    override fun check(item: CappedCoachingPlan): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class CappedCoachingPlanCreatedAtRangeRule(var min: Int, var max: Int) : CappedCoachingPlanRule {
    override fun check(item: CappedCoachingPlan): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingPlanTitleLengthRule(var maxLength: Int) : CappedCoachingPlanRule {
    override fun check(item: CappedCoachingPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingPlanRequiredPhoneRule(var message: String) : CappedCoachingPlanRule {
    override fun check(item: CappedCoachingPlan): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCoachingPlanAllowedPhoneRule(var allowed: MutableList<String>) : CappedCoachingPlanRule {
    override fun check(item: CappedCoachingPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped coaching plan. */
data class CappedCoachingPlanValidator(var rules: MutableList<CappedCoachingPlanRule>) {
    fun validate(item: CappedCoachingPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCoachingPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCoachingPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCoachingPlanValidator(): CappedCoachingPlanValidator {
    return CappedCoachingPlanValidator(
        mutableListOf<CappedCoachingPlanRule>(
            CappedCoachingPlanBatchSizeRangeRule(1000, 86400),
            CappedCoachingPlanCreatedAtRangeRule(32, 255),
            CappedCoachingPlanTitleLengthRule(80),
            CappedCoachingPlanRequiredPhoneRule("capped coaching plan needs this field"),
            CappedCoachingPlanAllowedPhoneRule(mutableListOf<String>("batched")),
        ),
    )
}
