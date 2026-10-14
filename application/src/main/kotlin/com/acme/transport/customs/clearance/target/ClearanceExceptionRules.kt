package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.text.normalize

/** A rule a clearance exception must pass. Check returns its problems, or none. */
interface ClearanceExceptionRule {
    fun check(item: ClearanceException): MutableList<String>
}

data class ClearanceExceptionNameLengthRule(var maxLength: Int) : ClearanceExceptionRule {
    override fun check(item: ClearanceException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceExceptionExpiresAtRangeRule(var min: Int, var max: Int) : ClearanceExceptionRule {
    override fun check(item: ClearanceException): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceExceptionAllowedNameRule(var allowed: MutableList<String>) : ClearanceExceptionRule {
    override fun check(item: ClearanceException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceExceptionCreatedAtRangeRule(var min: Int, var max: Int) : ClearanceExceptionRule {
    override fun check(item: ClearanceException): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a clearance exception. */
data class ClearanceExceptionValidator(var rules: MutableList<ClearanceExceptionRule>) {
    fun validate(item: ClearanceException): MutableList<String> {
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

    fun requireValid(item: ClearanceException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceException>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceExceptionValidator(): ClearanceExceptionValidator {
    return ClearanceExceptionValidator(
        mutableListOf<ClearanceExceptionRule>(
            ClearanceExceptionNameLengthRule(120),
            ClearanceExceptionExpiresAtRangeRule(32, 64),
            ClearanceExceptionAllowedNameRule(mutableListOf<String>()),
            ClearanceExceptionCreatedAtRangeRule(16, 255),
        ),
    )
}
