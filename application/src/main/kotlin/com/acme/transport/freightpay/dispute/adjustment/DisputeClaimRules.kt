package com.acme.transport.freightpay.dispute.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a dispute claim must pass. Check returns its problems, or none. */
interface DisputeClaimRule {
    fun check(item: DisputeClaim): MutableList<String>
}

data class DisputeClaimSourceLengthRule(var maxLength: Int) : DisputeClaimRule {
    override fun check(item: DisputeClaim): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeClaimRequiredSourceRule(var message: String) : DisputeClaimRule {
    override fun check(item: DisputeClaim): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeClaimRequiredSummaryRule(var message: String) : DisputeClaimRule {
    override fun check(item: DisputeClaim): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeClaimAllowedSourceRule(var allowed: MutableList<String>) : DisputeClaimRule {
    override fun check(item: DisputeClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DisputeClaimLabelLengthRule(var maxLength: Int) : DisputeClaimRule {
    override fun check(item: DisputeClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute claim. */
data class DisputeClaimValidator(var rules: MutableList<DisputeClaimRule>) {
    fun validate(item: DisputeClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DisputeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeClaimValidator(): DisputeClaimValidator {
    return DisputeClaimValidator(
        mutableListOf<DisputeClaimRule>(
            DisputeClaimSourceLengthRule(64),
            DisputeClaimRequiredSourceRule("dispute claim needs a value"),
            DisputeClaimRequiredSummaryRule("dispute claim needs an entry"),
            DisputeClaimAllowedSourceRule(mutableListOf<String>()),
            DisputeClaimLabelLengthRule(255),
        ),
    )
}
