package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize

/** A rule a accessorial target must pass. Check returns its problems, or none. */
interface AccessorialTargetRule {
    fun check(item: AccessorialTarget): MutableList<String>
}

data class AccessorialTargetCodeLengthRule(var maxLength: Int) : AccessorialTargetRule {
    override fun check(item: AccessorialTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialTargetDistanceKmRangeRule(var min: Double, var max: Double) : AccessorialTargetRule {
    override fun check(item: AccessorialTarget): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class AccessorialTargetAllowedDescriptionRule(var allowed: MutableList<String>) : AccessorialTargetRule {
    override fun check(item: AccessorialTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial target. */
data class AccessorialTargetValidator(var rules: MutableList<AccessorialTargetRule>) {
    fun validate(item: AccessorialTarget): MutableList<String> {
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

    fun requireValid(item: AccessorialTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialTargetValidator(): AccessorialTargetValidator {
    return AccessorialTargetValidator(
        mutableListOf<AccessorialTargetRule>(
            AccessorialTargetCodeLengthRule(1000),
            AccessorialTargetDistanceKmRangeRule(7.5, 4745.9),
            AccessorialTargetAllowedDescriptionRule(mutableListOf<String>("batched", "batched", "scheduled")),
        ),
    )
}
