package com.acme.transport.terminal.dwell.exception

/** A rule a global dwell exception must pass. Check returns its problems, or none. */
interface GlobalDwellExceptionRule {
    fun check(item: GlobalDwellException): MutableList<String>
}

data class GlobalDwellExceptionRequiredTimezoneRule(var message: String) : GlobalDwellExceptionRule {
    override fun check(item: GlobalDwellException): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellExceptionRequiredTitleRule(var message: String) : GlobalDwellExceptionRule {
    override fun check(item: GlobalDwellException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDwellExceptionVersionRangeRule(var min: Int, var max: Int) : GlobalDwellExceptionRule {
    override fun check(item: GlobalDwellException): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global dwell exception. */
data class GlobalDwellExceptionValidator(var rules: MutableList<GlobalDwellExceptionRule>) {
    fun validate(item: GlobalDwellException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDwellException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDwellException>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDwellExceptionValidator(): GlobalDwellExceptionValidator {
    return GlobalDwellExceptionValidator(
        mutableListOf<GlobalDwellExceptionRule>(
            GlobalDwellExceptionRequiredTimezoneRule("global dwell exception needs an entry"),
            GlobalDwellExceptionRequiredTitleRule("global dwell exception needs this field"),
            GlobalDwellExceptionVersionRangeRule(80, 80),
        ),
    )
}
