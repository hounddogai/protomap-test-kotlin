package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.text.normalize

/** A rule a legacy drop exception must pass. Check returns its problems, or none. */
interface LegacyDropExceptionRule {
    fun check(item: LegacyDropException): MutableList<String>
}

data class LegacyDropExceptionWeightRangeRule(var min: Double, var max: Double) : LegacyDropExceptionRule {
    override fun check(item: LegacyDropException): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDropExceptionDockLengthRule(var maxLength: Int) : LegacyDropExceptionRule {
    override fun check(item: LegacyDropException): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDropExceptionRequiredCodeRule(var message: String) : LegacyDropExceptionRule {
    override fun check(item: LegacyDropException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDropExceptionAllowedDockRule(var allowed: MutableList<String>) : LegacyDropExceptionRule {
    override fun check(item: LegacyDropException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDropExceptionCodeLengthRule(var maxLength: Int) : LegacyDropExceptionRule {
    override fun check(item: LegacyDropException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy drop exception. */
data class LegacyDropExceptionValidator(var rules: MutableList<LegacyDropExceptionRule>) {
    fun validate(item: LegacyDropException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDropException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDropException>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDropExceptionValidator(): LegacyDropExceptionValidator {
    return LegacyDropExceptionValidator(
        mutableListOf<LegacyDropExceptionRule>(
            LegacyDropExceptionWeightRangeRule(3.1, 2919.8),
            LegacyDropExceptionDockLengthRule(86400),
            LegacyDropExceptionRequiredCodeRule("legacy drop exception needs this field"),
            LegacyDropExceptionAllowedDockRule(mutableListOf<String>("adaptive")),
            LegacyDropExceptionCodeLengthRule(3600),
        ),
    )
}
