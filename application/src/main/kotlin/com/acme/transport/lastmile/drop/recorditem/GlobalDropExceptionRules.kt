package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a global drop exception must pass. Check returns its problems, or none. */
interface GlobalDropExceptionRule {
    fun check(item: GlobalDropException): MutableList<String>
}

data class GlobalDropExceptionAllowedCodeRule(var allowed: MutableList<String>) : GlobalDropExceptionRule {
    override fun check(item: GlobalDropException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropExceptionExpiresAtRangeRule(var min: Int, var max: Int) : GlobalDropExceptionRule {
    override fun check(item: GlobalDropException): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropExceptionCreatedAtRangeRule(var min: Int, var max: Int) : GlobalDropExceptionRule {
    override fun check(item: GlobalDropException): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropExceptionRequiredLaneRule(var message: String) : GlobalDropExceptionRule {
    override fun check(item: GlobalDropException): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropExceptionCodeLengthRule(var maxLength: Int) : GlobalDropExceptionRule {
    override fun check(item: GlobalDropException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDropExceptionRequiredCodeRule(var message: String) : GlobalDropExceptionRule {
    override fun check(item: GlobalDropException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global drop exception. */
data class GlobalDropExceptionValidator(var rules: MutableList<GlobalDropExceptionRule>) {
    fun validate(item: GlobalDropException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDropException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDropException>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDropExceptionValidator(): GlobalDropExceptionValidator {
    return GlobalDropExceptionValidator(
        mutableListOf<GlobalDropExceptionRule>(
            GlobalDropExceptionAllowedCodeRule(mutableListOf<String>()),
            GlobalDropExceptionExpiresAtRangeRule(16, 120),
            GlobalDropExceptionCreatedAtRangeRule(3600, 3600),
            GlobalDropExceptionRequiredLaneRule("global drop exception needs an entry"),
            GlobalDropExceptionCodeLengthRule(255),
            GlobalDropExceptionRequiredCodeRule("global drop exception needs this field"),
        ),
    )
}
