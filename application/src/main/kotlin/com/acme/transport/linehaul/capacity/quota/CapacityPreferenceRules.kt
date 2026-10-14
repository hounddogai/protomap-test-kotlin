package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.text.normalize

/** A rule a capacity preference must pass. Check returns its problems, or none. */
interface CapacityPreferenceRule {
    fun check(item: CapacityPreference): MutableList<String>
}

data class CapacityPreferenceExpiresAtRangeRule(var min: Int, var max: Int) : CapacityPreferenceRule {
    override fun check(item: CapacityPreference): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityPreferenceAllowedCategoryRule(var allowed: MutableList<String>) : CapacityPreferenceRule {
    override fun check(item: CapacityPreference): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CapacityPreferenceAttemptsRangeRule(var min: Int, var max: Int) : CapacityPreferenceRule {
    override fun check(item: CapacityPreference): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CapacityPreferenceCodeLengthRule(var maxLength: Int) : CapacityPreferenceRule {
    override fun check(item: CapacityPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity preference. */
data class CapacityPreferenceValidator(var rules: MutableList<CapacityPreferenceRule>) {
    fun validate(item: CapacityPreference): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityPreferenceValidator(): CapacityPreferenceValidator {
    return CapacityPreferenceValidator(
        mutableListOf<CapacityPreferenceRule>(
            CapacityPreferenceExpiresAtRangeRule(255, 86400),
            CapacityPreferenceAllowedCategoryRule(mutableListOf<String>("capped", "global")),
            CapacityPreferenceAttemptsRangeRule(255, 86400),
            CapacityPreferenceCodeLengthRule(80),
        ),
    )
}
