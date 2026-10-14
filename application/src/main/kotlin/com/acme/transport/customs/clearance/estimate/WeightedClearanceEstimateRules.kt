package com.acme.transport.customs.clearance.estimate

import com.acme.transport.kit.text.normalize

/** A rule a weighted clearance estimate must pass. Check returns its problems, or none. */
interface WeightedClearanceEstimateRule {
    fun check(item: WeightedClearanceEstimate): MutableList<String>
}

data class WeightedClearanceEstimateRequiredNameRule(var message: String) : WeightedClearanceEstimateRule {
    override fun check(item: WeightedClearanceEstimate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedClearanceEstimateNameLengthRule(var maxLength: Int) : WeightedClearanceEstimateRule {
    override fun check(item: WeightedClearanceEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedClearanceEstimateActiveGuardRule(var limit: Int) : WeightedClearanceEstimateRule {
    override fun check(item: WeightedClearanceEstimate): MutableList<String> {
        if (item.active && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class WeightedClearanceEstimateRequiredCodeRule(var message: String) : WeightedClearanceEstimateRule {
    override fun check(item: WeightedClearanceEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedClearanceEstimateAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedClearanceEstimateRule {
    override fun check(item: WeightedClearanceEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class WeightedClearanceEstimateAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : WeightedClearanceEstimateRule {
    override fun check(item: WeightedClearanceEstimate): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted clearance estimate. */
data class WeightedClearanceEstimateValidator(var rules: MutableList<WeightedClearanceEstimateRule>) {
    fun validate(item: WeightedClearanceEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedClearanceEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedClearanceEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedClearanceEstimateValidator(): WeightedClearanceEstimateValidator {
    return WeightedClearanceEstimateValidator(
        mutableListOf<WeightedClearanceEstimateRule>(
            WeightedClearanceEstimateRequiredNameRule("weighted clearance estimate needs a value"),
            WeightedClearanceEstimateNameLengthRule(80),
            WeightedClearanceEstimateActiveGuardRule(255),
            WeightedClearanceEstimateRequiredCodeRule("weighted clearance estimate needs a value"),
            WeightedClearanceEstimateAllowedNameRule(mutableListOf<String>()),
            WeightedClearanceEstimateAttemptsRangeRule(3600, 86400),
        ),
    )
}
