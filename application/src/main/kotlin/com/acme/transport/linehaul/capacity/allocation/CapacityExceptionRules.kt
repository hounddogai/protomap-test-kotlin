package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize

/** A rule a capacity exception must pass. Check returns its problems, or none. */
interface CapacityExceptionRule {
    fun check(item: CapacityException): MutableList<String>
}

data class CapacityExceptionNameLengthRule(var maxLength: Int) : CapacityExceptionRule {
    override fun check(item: CapacityException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityExceptionRequiredChannelRule(var message: String) : CapacityExceptionRule {
    override fun check(item: CapacityException): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityExceptionAllowedChannelRule(var allowed: MutableList<String>) : CapacityExceptionRule {
    override fun check(item: CapacityException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity exception. */
data class CapacityExceptionValidator(var rules: MutableList<CapacityExceptionRule>) {
    fun validate(item: CapacityException): MutableList<String> {
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

    fun requireValid(item: CapacityException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityException>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityExceptionValidator(): CapacityExceptionValidator {
    return CapacityExceptionValidator(
        mutableListOf<CapacityExceptionRule>(
            CapacityExceptionNameLengthRule(86400),
            CapacityExceptionRequiredChannelRule("capacity exception needs this field"),
            CapacityExceptionAllowedChannelRule(mutableListOf<String>()),
        ),
    )
}
