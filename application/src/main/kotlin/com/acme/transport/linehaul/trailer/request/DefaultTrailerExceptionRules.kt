package com.acme.transport.linehaul.trailer.request

/** A rule a default trailer exception must pass. Check returns its problems, or none. */
interface DefaultTrailerExceptionRule {
    fun check(item: DefaultTrailerException): MutableList<String>
}

data class DefaultTrailerExceptionExpiresAtRangeRule(var min: Int, var max: Int) : DefaultTrailerExceptionRule {
    override fun check(item: DefaultTrailerException): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerExceptionLabelLengthRule(var maxLength: Int) : DefaultTrailerExceptionRule {
    override fun check(item: DefaultTrailerException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerExceptionCarrierLengthRule(var maxLength: Int) : DefaultTrailerExceptionRule {
    override fun check(item: DefaultTrailerException): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default trailer exception. */
data class DefaultTrailerExceptionValidatorExtra(var rules: MutableList<DefaultTrailerExceptionRule>) {
    fun validate(item: DefaultTrailerException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTrailerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTrailerException>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTrailerExceptionValidator(): DefaultTrailerExceptionValidatorExtra {
    return DefaultTrailerExceptionValidatorExtra(
        mutableListOf<DefaultTrailerExceptionRule>(
            DefaultTrailerExceptionExpiresAtRangeRule(80, 86400),
            DefaultTrailerExceptionLabelLengthRule(64),
            DefaultTrailerExceptionCarrierLengthRule(255),
        ),
    )
}
