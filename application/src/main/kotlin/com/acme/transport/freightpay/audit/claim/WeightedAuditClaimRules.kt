package com.acme.transport.freightpay.audit.claim

/** A rule a weighted audit claim must pass. Check returns its problems, or none. */
interface WeightedAuditClaimRule {
    fun check(item: WeightedAuditClaim): MutableList<String>
}

data class WeightedAuditClaimRequiredExternalRefRule(var message: String) : WeightedAuditClaimRule {
    override fun check(item: WeightedAuditClaim): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAuditClaimLoadKgRangeRule(var min: Double, var max: Double) : WeightedAuditClaimRule {
    override fun check(item: WeightedAuditClaim): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class WeightedAuditClaimCreatedAtRangeRule(var min: Int, var max: Int) : WeightedAuditClaimRule {
    override fun check(item: WeightedAuditClaim): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class WeightedAuditClaimRequiredCodeRule(var message: String) : WeightedAuditClaimRule {
    override fun check(item: WeightedAuditClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted audit claim. */
data class WeightedAuditClaimValidator(var rules: MutableList<WeightedAuditClaimRule>) {
    fun validate(item: WeightedAuditClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAuditClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAuditClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAuditClaimValidator(): WeightedAuditClaimValidator {
    return WeightedAuditClaimValidator(
        mutableListOf<WeightedAuditClaimRule>(
            WeightedAuditClaimRequiredExternalRefRule("weighted audit claim needs an entry"),
            WeightedAuditClaimLoadKgRangeRule(9.0, 1811.7),
            WeightedAuditClaimCreatedAtRangeRule(64, 3600),
            WeightedAuditClaimRequiredCodeRule("weighted audit claim needs this field"),
        ),
    )
}
