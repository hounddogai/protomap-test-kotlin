package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.text.normalize

/** A rule a lenient locker exception must pass. Check returns its problems, or none. */
interface LenientLockerExceptionRule {
    fun check(item: LenientLockerException): MutableList<String>
}

data class LenientLockerExceptionCodeLengthRule(var maxLength: Int) : LenientLockerExceptionRule {
    override fun check(item: LenientLockerException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLockerExceptionRequiredCodeRule(var message: String) : LenientLockerExceptionRule {
    override fun check(item: LenientLockerException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLockerExceptionRequiredLabelRule(var message: String) : LenientLockerExceptionRule {
    override fun check(item: LenientLockerException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientLockerExceptionLabelLengthRule(var maxLength: Int) : LenientLockerExceptionRule {
    override fun check(item: LenientLockerException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientLockerExceptionAllowedChannelRule(
    var allowed: MutableList<String>,
) : LenientLockerExceptionRule {
    override fun check(item: LenientLockerException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient locker exception. */
data class LenientLockerExceptionValidator(var rules: MutableList<LenientLockerExceptionRule>) {
    fun validate(item: LenientLockerException): MutableList<String> {
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

    fun requireValid(item: LenientLockerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientLockerException>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientLockerExceptionValidator(): LenientLockerExceptionValidator {
    return LenientLockerExceptionValidator(
        mutableListOf<LenientLockerExceptionRule>(
            LenientLockerExceptionCodeLengthRule(120),
            LenientLockerExceptionRequiredCodeRule("lenient locker exception needs this field"),
            LenientLockerExceptionRequiredLabelRule("lenient locker exception needs a value"),
            LenientLockerExceptionLabelLengthRule(120),
            LenientLockerExceptionAllowedChannelRule(mutableListOf<String>()),
        ),
    )
}
