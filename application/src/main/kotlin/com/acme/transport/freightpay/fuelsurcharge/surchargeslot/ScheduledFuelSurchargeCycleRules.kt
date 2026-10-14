package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import com.acme.transport.kit.text.normalize

/** A rule a scheduled fuel surcharge cycle must pass. Check returns its problems, or none. */
interface ScheduledFuelSurchargeCycleRule {
    fun check(item: ScheduledFuelSurchargeCycle): MutableList<String>
}

data class ScheduledFuelSurchargeCycleStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledFuelSurchargeCycleRule {
    override fun check(item: ScheduledFuelSurchargeCycle): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ScheduledFuelSurchargeCycleAllowedReviewerRule(
    var allowed: MutableList<String>,
) : ScheduledFuelSurchargeCycleRule {
    override fun check(item: ScheduledFuelSurchargeCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelSurchargeCycleUnitLengthRule(var maxLength: Int) : ScheduledFuelSurchargeCycleRule {
    override fun check(item: ScheduledFuelSurchargeCycle): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel surcharge cycle. */
data class ScheduledFuelSurchargeCycleValidator(var rules: MutableList<ScheduledFuelSurchargeCycleRule>) {
    fun validate(item: ScheduledFuelSurchargeCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledFuelSurchargeCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelSurchargeCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelSurchargeCycleValidator(): ScheduledFuelSurchargeCycleValidator {
    return ScheduledFuelSurchargeCycleValidator(
        mutableListOf<ScheduledFuelSurchargeCycleRule>(
            ScheduledFuelSurchargeCycleStartsAtRangeRule(32, 255),
            ScheduledFuelSurchargeCycleAllowedReviewerRule(mutableListOf<String>("global", "capped")),
            ScheduledFuelSurchargeCycleUnitLengthRule(32),
        ),
    )
}
