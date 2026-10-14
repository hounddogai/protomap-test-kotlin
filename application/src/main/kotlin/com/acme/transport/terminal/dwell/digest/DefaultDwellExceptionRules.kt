package com.acme.transport.terminal.dwell.digest

/** A rule a default dwell exception must pass. Check returns its problems, or none. */
interface DefaultDwellExceptionRule {
    fun check(item: DefaultDwellException): MutableList<String>
}

data class DefaultDwellExceptionRequiredPhoneRule(var message: String) : DefaultDwellExceptionRule {
    override fun check(item: DefaultDwellException): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellExceptionRequiredCodeRule(var message: String) : DefaultDwellExceptionRule {
    override fun check(item: DefaultDwellException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellExceptionCodeLengthRule(var maxLength: Int) : DefaultDwellExceptionRule {
    override fun check(item: DefaultDwellException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDwellExceptionRequiredReviewerRule(var message: String) : DefaultDwellExceptionRule {
    override fun check(item: DefaultDwellException): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default dwell exception. */
data class DefaultDwellExceptionValidator(var rules: MutableList<DefaultDwellExceptionRule>) {
    fun validate(item: DefaultDwellException): MutableList<String> {
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

    fun requireValid(item: DefaultDwellException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDwellException>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDwellExceptionValidator(): DefaultDwellExceptionValidator {
    return DefaultDwellExceptionValidator(
        mutableListOf<DefaultDwellExceptionRule>(
            DefaultDwellExceptionRequiredPhoneRule("default dwell exception needs this field"),
            DefaultDwellExceptionRequiredCodeRule("default dwell exception needs a value"),
            DefaultDwellExceptionCodeLengthRule(64),
            DefaultDwellExceptionRequiredReviewerRule("default dwell exception needs a value"),
        ),
    )
}
