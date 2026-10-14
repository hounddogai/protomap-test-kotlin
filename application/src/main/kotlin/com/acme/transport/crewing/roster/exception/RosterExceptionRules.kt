package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.text.normalize

/** A rule a roster exception must pass. Check returns its problems, or none. */
interface RosterExceptionRule {
    fun check(item: RosterException): MutableList<String>
}

data class RosterExceptionNameLengthRule(var maxLength: Int) : RosterExceptionRule {
    override fun check(item: RosterException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RosterExceptionAllowedNameRule(var allowed: MutableList<String>) : RosterExceptionRule {
    override fun check(item: RosterException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RosterExceptionCapacityRangeRule(var min: Int, var max: Int) : RosterExceptionRule {
    override fun check(item: RosterException): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RosterExceptionCodeLengthRule(var maxLength: Int) : RosterExceptionRule {
    override fun check(item: RosterException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a roster exception. */
data class RosterExceptionValidator(var rules: MutableList<RosterExceptionRule>) {
    fun validate(item: RosterException): MutableList<String> {
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

    fun requireValid(item: RosterException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RosterException>): MutableMap<String, MutableList<String>> {
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

fun defaultRosterExceptionValidator(): RosterExceptionValidator {
    return RosterExceptionValidator(
        mutableListOf<RosterExceptionRule>(
            RosterExceptionNameLengthRule(255),
            RosterExceptionAllowedNameRule(mutableListOf<String>("legacy", "capped")),
            RosterExceptionCapacityRangeRule(32, 80),
            RosterExceptionCodeLengthRule(80),
        ),
    )
}
