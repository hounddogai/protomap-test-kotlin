package com.acme.transport.freightpay.dispute.window

/** A rule a dispute exception must pass. Check returns its problems, or none. */
interface DisputeExceptionRule {
    fun check(item: DisputeException): MutableList<String>
}

data class DisputeExceptionRequiredLabelRule(var message: String) : DisputeExceptionRule {
    override fun check(item: DisputeException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeExceptionExpiresAtRangeRule(var min: Int, var max: Int) : DisputeExceptionRule {
    override fun check(item: DisputeException): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class DisputeExceptionLabelLengthRule(var maxLength: Int) : DisputeExceptionRule {
    override fun check(item: DisputeException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute exception. */
data class DisputeExceptionValidator(var rules: MutableList<DisputeExceptionRule>) {
    fun validate(item: DisputeException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeException>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeExceptionValidator(): DisputeExceptionValidator {
    return DisputeExceptionValidator(
        mutableListOf<DisputeExceptionRule>(
            DisputeExceptionRequiredLabelRule("dispute exception needs an entry"),
            DisputeExceptionExpiresAtRangeRule(64, 120),
            DisputeExceptionLabelLengthRule(32),
        ),
    )
}
