package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.text.normalize

/** A rule a availability score must pass. Check returns its problems, or none. */
interface AvailabilityScoreRule {
    fun check(item: AvailabilityScore): MutableList<String>
}

data class AvailabilityScoreDiscountRangeRule(var min: Double, var max: Double) : AvailabilityScoreRule {
    override fun check(item: AvailabilityScore): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityScoreCodeLengthRule(var maxLength: Int) : AvailabilityScoreRule {
    override fun check(item: AvailabilityScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityScoreAllowedTitleRule(var allowed: MutableList<String>) : AvailabilityScoreRule {
    override fun check(item: AvailabilityScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityScoreLaneLengthRule(var maxLength: Int) : AvailabilityScoreRule {
    override fun check(item: AvailabilityScore): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability score. */
data class AvailabilityScoreValidator(var rules: MutableList<AvailabilityScoreRule>) {
    fun validate(item: AvailabilityScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityScore>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityScoreValidator(): AvailabilityScoreValidator {
    return AvailabilityScoreValidator(
        mutableListOf<AvailabilityScoreRule>(
            AvailabilityScoreDiscountRangeRule(3.7, 3635.2),
            AvailabilityScoreCodeLengthRule(16),
            AvailabilityScoreAllowedTitleRule(mutableListOf<String>("capped", "regional")),
            AvailabilityScoreLaneLengthRule(32),
        ),
    )
}
