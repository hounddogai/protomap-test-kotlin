package com.acme.transport.freightpay.accessorial.metric

import com.acme.transport.kit.text.normalize

/** A rule a accessorial exception must pass. Check returns its problems, or none. */
interface AccessorialExceptionRule {
    fun check(item: AccessorialException): MutableList<String>
}

data class AccessorialExceptionCodePrefixRule(var prefix: String) : AccessorialExceptionRule {
    override fun check(item: AccessorialException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialExceptionRequiredLabelRule(var message: String) : AccessorialExceptionRule {
    override fun check(item: AccessorialException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AccessorialExceptionCodeLengthRule(var maxLength: Int) : AccessorialExceptionRule {
    override fun check(item: AccessorialException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialExceptionAllowedLaneRule(var allowed: MutableList<String>) : AccessorialExceptionRule {
    override fun check(item: AccessorialException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial exception. */
data class AccessorialExceptionValidator(var rules: MutableList<AccessorialExceptionRule>) {
    fun validate(item: AccessorialException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialException>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialExceptionValidator(): AccessorialExceptionValidator {
    return AccessorialExceptionValidator(
        mutableListOf<AccessorialExceptionRule>(
            AccessorialExceptionCodePrefixRule("Q"),
            AccessorialExceptionRequiredLabelRule("accessorial exception needs a value"),
            AccessorialExceptionCodeLengthRule(80),
            AccessorialExceptionAllowedLaneRule(mutableListOf<String>("manual", "manual", "primary")),
        ),
    )
}
