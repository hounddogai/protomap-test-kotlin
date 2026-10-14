package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.normalize

/** A rule a scheduled qualification profile must pass. Check returns its problems, or none. */
interface ScheduledQualificationProfileRule {
    fun check(item: ScheduledQualificationProfile): MutableList<String>
}

data class ScheduledQualificationProfileQuantityRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledQualificationProfileRule {
    override fun check(item: ScheduledQualificationProfile): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class ScheduledQualificationProfileCodeLengthRule(var maxLength: Int) : ScheduledQualificationProfileRule {
    override fun check(item: ScheduledQualificationProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationProfileExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledQualificationProfileRule {
    override fun check(item: ScheduledQualificationProfile): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationProfileRequiredCodeRule(
    var message: String,
) : ScheduledQualificationProfileRule {
    override fun check(item: ScheduledQualificationProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledQualificationProfileAllowedCodeRule(
    var allowed: MutableList<String>,
) : ScheduledQualificationProfileRule {
    override fun check(item: ScheduledQualificationProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled qualification profile. */
data class ScheduledQualificationProfileValidator(var rules: MutableList<ScheduledQualificationProfileRule>) {
    fun validate(item: ScheduledQualificationProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledQualificationProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledQualificationProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledQualificationProfileValidator(): ScheduledQualificationProfileValidator {
    return ScheduledQualificationProfileValidator(
        mutableListOf<ScheduledQualificationProfileRule>(
            ScheduledQualificationProfileQuantityRangeRule(255, 1000),
            ScheduledQualificationProfileCodeLengthRule(1000),
            ScheduledQualificationProfileExpiresAtRangeRule(16, 255),
            ScheduledQualificationProfileRequiredCodeRule("scheduled qualification profile needs this field"),
            ScheduledQualificationProfileAllowedCodeRule(mutableListOf<String>("batched", "adaptive")),
        ),
    )
}
