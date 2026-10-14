package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.text.normalize

/** A rule a capped locker exception must pass. Check returns its problems, or none. */
interface CappedLockerExceptionRule {
    fun check(item: CappedLockerException): MutableList<String>
}

data class CappedLockerExceptionRequiredCodeRule(var message: String) : CappedLockerExceptionRule {
    override fun check(item: CappedLockerException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerExceptionLabelLengthRule(var maxLength: Int) : CappedLockerExceptionRule {
    override fun check(item: CappedLockerException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerExceptionRequiredLabelRule(var message: String) : CappedLockerExceptionRule {
    override fun check(item: CappedLockerException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerExceptionQuantityRangeRule(var min: Int, var max: Int) : CappedLockerExceptionRule {
    override fun check(item: CappedLockerException): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerExceptionAllowedSummaryRule(
    var allowed: MutableList<String>,
) : CappedLockerExceptionRule {
    override fun check(item: CappedLockerException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class CappedLockerExceptionCodeLengthRule(var maxLength: Int) : CappedLockerExceptionRule {
    override fun check(item: CappedLockerException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped locker exception. */
data class CappedLockerExceptionValidator(var rules: MutableList<CappedLockerExceptionRule>) {
    fun validate(item: CappedLockerException): MutableList<String> {
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

    fun requireValid(item: CappedLockerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedLockerException>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedLockerExceptionValidator(): CappedLockerExceptionValidator {
    return CappedLockerExceptionValidator(
        mutableListOf<CappedLockerExceptionRule>(
            CappedLockerExceptionRequiredCodeRule("capped locker exception needs an entry"),
            CappedLockerExceptionLabelLengthRule(16),
            CappedLockerExceptionRequiredLabelRule("capped locker exception needs a value"),
            CappedLockerExceptionQuantityRangeRule(64, 1000),
            CappedLockerExceptionAllowedSummaryRule(mutableListOf<String>("legacy", "weighted")),
            CappedLockerExceptionCodeLengthRule(80),
        ),
    )
}
