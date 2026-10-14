package com.acme.transport.freightpay.dispute.digest

/** A rule a dispute digest must pass. Check returns its problems, or none. */
interface DisputeDigestRule {
    fun check(item: DisputeDigest): MutableList<String>
}

data class DisputeDigestAttemptsRangeRule(var min: Int, var max: Int) : DisputeDigestRule {
    override fun check(item: DisputeDigest): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeDigestRequiredLabelRule(var message: String) : DisputeDigestRule {
    override fun check(item: DisputeDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeDigestRequiredCodeRule(var message: String) : DisputeDigestRule {
    override fun check(item: DisputeDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute digest. */
data class DisputeDigestValidator(var rules: MutableList<DisputeDigestRule>) {
    fun validate(item: DisputeDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeDigestValidator(): DisputeDigestValidator {
    return DisputeDigestValidator(
        mutableListOf<DisputeDigestRule>(
            DisputeDigestAttemptsRangeRule(64, 120),
            DisputeDigestRequiredLabelRule("dispute digest needs a value"),
            DisputeDigestRequiredCodeRule("dispute digest needs this field"),
        ),
    )
}
