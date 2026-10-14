package com.acme.transport.terminal.dwell.preference

/** A rule a fallback dwell exception must pass. Check returns its problems, or none. */
interface FallbackDwellExceptionRule {
    fun check(item: FallbackDwellException): MutableList<String>
}

data class FallbackDwellExceptionRequiredTitleRule(var message: String) : FallbackDwellExceptionRule {
    override fun check(item: FallbackDwellException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellExceptionRequiredCodeRule(var message: String) : FallbackDwellExceptionRule {
    override fun check(item: FallbackDwellException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellExceptionRouteCodeLengthRule(var maxLength: Int) : FallbackDwellExceptionRule {
    override fun check(item: FallbackDwellException): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellExceptionRequiredRouteCodeRule(var message: String) : FallbackDwellExceptionRule {
    override fun check(item: FallbackDwellException): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackDwellExceptionStartsAtRangeRule(var min: Int, var max: Int) : FallbackDwellExceptionRule {
    override fun check(item: FallbackDwellException): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dwell exception. */
data class FallbackDwellExceptionValidator(var rules: MutableList<FallbackDwellExceptionRule>) {
    fun validate(item: FallbackDwellException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDwellException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDwellException>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDwellExceptionValidator(): FallbackDwellExceptionValidator {
    return FallbackDwellExceptionValidator(
        mutableListOf<FallbackDwellExceptionRule>(
            FallbackDwellExceptionRequiredTitleRule("fallback dwell exception needs an entry"),
            FallbackDwellExceptionRequiredCodeRule("fallback dwell exception needs this field"),
            FallbackDwellExceptionRouteCodeLengthRule(255),
            FallbackDwellExceptionRequiredRouteCodeRule("fallback dwell exception needs an entry"),
            FallbackDwellExceptionStartsAtRangeRule(120, 3600),
        ),
    )
}
