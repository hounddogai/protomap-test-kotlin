package com.acme.transport.freightpay.dispute.manifest

/** A rule a pending dispute claim must pass. Check returns its problems, or none. */
interface PendingDisputeClaimRule {
    fun check(item: PendingDisputeClaim): MutableList<String>
}

data class PendingDisputeClaimRequiredNameRule(var message: String) : PendingDisputeClaimRule {
    override fun check(item: PendingDisputeClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDisputeClaimExpiresAtRangeRule(var min: Int, var max: Int) : PendingDisputeClaimRule {
    override fun check(item: PendingDisputeClaim): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingDisputeClaimRequiredTimezoneRule(var message: String) : PendingDisputeClaimRule {
    override fun check(item: PendingDisputeClaim): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending dispute claim. */
data class PendingDisputeClaimValidator(var rules: MutableList<PendingDisputeClaimRule>) {
    fun validate(item: PendingDisputeClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDisputeClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDisputeClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDisputeClaimValidator(): PendingDisputeClaimValidator {
    return PendingDisputeClaimValidator(
        mutableListOf<PendingDisputeClaimRule>(
            PendingDisputeClaimRequiredNameRule("pending dispute claim needs this field"),
            PendingDisputeClaimExpiresAtRangeRule(120, 120),
            PendingDisputeClaimRequiredTimezoneRule("pending dispute claim needs this field"),
        ),
    )
}
