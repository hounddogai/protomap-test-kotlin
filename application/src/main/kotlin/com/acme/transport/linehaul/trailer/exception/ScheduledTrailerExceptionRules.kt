package com.acme.transport.linehaul.trailer.exception

/** A rule a scheduled trailer exception must pass. Check returns its problems, or none. */
interface ScheduledTrailerExceptionRule {
    fun check(item: ScheduledTrailerException): MutableList<String>
}

data class ScheduledTrailerExceptionRequiredExternalRefRule(
    var message: String,
) : ScheduledTrailerExceptionRule {
    override fun check(item: ScheduledTrailerException): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerExceptionUnitLengthRule(var maxLength: Int) : ScheduledTrailerExceptionRule {
    override fun check(item: ScheduledTrailerException): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerExceptionRequiredLabelRule(var message: String) : ScheduledTrailerExceptionRule {
    override fun check(item: ScheduledTrailerException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer exception. */
data class ScheduledTrailerExceptionValidator(var rules: MutableList<ScheduledTrailerExceptionRule>) {
    fun validate(item: ScheduledTrailerException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTrailerException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerException>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerExceptionValidator(): ScheduledTrailerExceptionValidator {
    return ScheduledTrailerExceptionValidator(
        mutableListOf<ScheduledTrailerExceptionRule>(
            ScheduledTrailerExceptionRequiredExternalRefRule("scheduled trailer exception needs a value"),
            ScheduledTrailerExceptionUnitLengthRule(86400),
            ScheduledTrailerExceptionRequiredLabelRule("scheduled trailer exception needs an entry"),
        ),
    )
}
