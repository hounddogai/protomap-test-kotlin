package com.acme.transport.network.transittime.timeoverride

/** A rule a default transit time exception must pass. Check returns its problems, or none. */
interface DefaultTransitTimeExceptionRule {
    fun check(item: DefaultTransitTimeException): MutableList<String>
}

data class DefaultTransitTimeExceptionRequiredNameRule(var message: String) : DefaultTransitTimeExceptionRule {
    override fun check(item: DefaultTransitTimeException): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeExceptionSequenceRangeRule(
    var min: Int,
    var max: Int,
) : DefaultTransitTimeExceptionRule {
    override fun check(item: DefaultTransitTimeException): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTransitTimeExceptionCodeLengthRule(var maxLength: Int) : DefaultTransitTimeExceptionRule {
    override fun check(item: DefaultTransitTimeException): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default transit time exception. */
data class DefaultTransitTimeExceptionValidator(var rules: MutableList<DefaultTransitTimeExceptionRule>) {
    fun validate(item: DefaultTransitTimeException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTransitTimeException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTransitTimeException>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTransitTimeExceptionValidator(): DefaultTransitTimeExceptionValidator {
    return DefaultTransitTimeExceptionValidator(
        mutableListOf<DefaultTransitTimeExceptionRule>(
            DefaultTransitTimeExceptionRequiredNameRule("default transit time exception needs a value"),
            DefaultTransitTimeExceptionSequenceRangeRule(3600, 86400),
            DefaultTransitTimeExceptionCodeLengthRule(3600),
        ),
    )
}
