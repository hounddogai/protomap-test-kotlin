package com.acme.transport.fleetops.tire.exception

import com.acme.transport.kit.text.normalize

/** A rule a capped tire exception must pass. Check returns its problems, or none. */
interface CappedTireExceptionRule {
    fun check(item: CappedTireException): MutableList<String>
}

data class CappedTireExceptionCodeLengthRule(var maxLength: Int) : CappedTireExceptionRule {
    override fun check(item: CappedTireException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireExceptionAllowedLabelRule(var allowed: MutableList<String>) : CappedTireExceptionRule {
    override fun check(item: CappedTireException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireExceptionDescriptionLengthRule(var maxLength: Int) : CappedTireExceptionRule {
    override fun check(item: CappedTireException): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireExceptionRequiredDescriptionRule(var message: String) : CappedTireExceptionRule {
    override fun check(item: CappedTireException): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tire exception. */
data class CappedTireExceptionValidator(var rules: MutableList<CappedTireExceptionRule>) {
    fun validate(item: CappedTireException): MutableList<String> {
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

    fun requireValid(item: CappedTireException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTireException>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTireExceptionValidator(): CappedTireExceptionValidator {
    return CappedTireExceptionValidator(
        mutableListOf<CappedTireExceptionRule>(
            CappedTireExceptionCodeLengthRule(1000),
            CappedTireExceptionAllowedLabelRule(mutableListOf<String>("legacy")),
            CappedTireExceptionDescriptionLengthRule(64),
            CappedTireExceptionRequiredDescriptionRule("capped tire exception needs a value"),
        ),
    )
}
