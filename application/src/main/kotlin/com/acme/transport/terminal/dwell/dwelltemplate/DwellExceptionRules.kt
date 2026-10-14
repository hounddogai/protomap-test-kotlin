package com.acme.transport.terminal.dwell.dwelltemplate

/** A rule a dwell exception must pass. Check returns its problems, or none. */
interface DwellExceptionRule {
    fun check(item: DwellException): MutableList<String>
}

data class DwellExceptionRequiredNameRule(var message: String) : DwellExceptionRule {
    override fun check(item: DwellException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellExceptionRequiredExternalRefRule(var message: String) : DwellExceptionRule {
    override fun check(item: DwellException): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DwellExceptionCodeLengthRule(var maxLength: Int) : DwellExceptionRule {
    override fun check(item: DwellException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DwellExceptionExternalRefLengthRule(var maxLength: Int) : DwellExceptionRule {
    override fun check(item: DwellException): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dwell exception. */
data class DwellExceptionValidator(var rules: MutableList<DwellExceptionRule>) {
    fun validate(item: DwellException): MutableList<String> {
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

    fun requireValid(item: DwellException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DwellException>): MutableMap<String, MutableList<String>> {
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

fun defaultDwellExceptionValidator(): DwellExceptionValidator {
    return DwellExceptionValidator(
        mutableListOf<DwellExceptionRule>(
            DwellExceptionRequiredNameRule("dwell exception needs an entry"),
            DwellExceptionRequiredExternalRefRule("dwell exception needs an entry"),
            DwellExceptionCodeLengthRule(16),
            DwellExceptionExternalRefLengthRule(32),
        ),
    )
}
