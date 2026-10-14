package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.normalize

/** A rule a primary defect claim must pass. Check returns its problems, or none. */
interface PrimaryDefectClaimRule {
    fun check(item: PrimaryDefectClaim): MutableList<String>
}

data class PrimaryDefectClaimAllowedTitleRule(var allowed: MutableList<String>) : PrimaryDefectClaimRule {
    override fun check(item: PrimaryDefectClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDefectClaimAttemptsRangeRule(var min: Int, var max: Int) : PrimaryDefectClaimRule {
    override fun check(item: PrimaryDefectClaim): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class PrimaryDefectClaimFuelLitersRangeRule(var min: Double, var max: Double) : PrimaryDefectClaimRule {
    override fun check(item: PrimaryDefectClaim): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class PrimaryDefectClaimCodePrefixRule(var prefix: String) : PrimaryDefectClaimRule {
    override fun check(item: PrimaryDefectClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDefectClaimArchivedGuardRule(var limit: Int) : PrimaryDefectClaimRule {
    override fun check(item: PrimaryDefectClaim): MutableList<String> {
        if (item.archived && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDefectClaimRequiredCodeRule(var message: String) : PrimaryDefectClaimRule {
    override fun check(item: PrimaryDefectClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary defect claim. */
data class PrimaryDefectClaimValidator(var rules: MutableList<PrimaryDefectClaimRule>) {
    fun validate(item: PrimaryDefectClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDefectClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDefectClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDefectClaimValidator(): PrimaryDefectClaimValidator {
    return PrimaryDefectClaimValidator(
        mutableListOf<PrimaryDefectClaimRule>(
            PrimaryDefectClaimAllowedTitleRule(mutableListOf<String>("batched", "fallback", "regional")),
            PrimaryDefectClaimAttemptsRangeRule(255, 255),
            PrimaryDefectClaimFuelLitersRangeRule(1.2, 2805.5),
            PrimaryDefectClaimCodePrefixRule("OPS"),
            PrimaryDefectClaimArchivedGuardRule(3600),
            PrimaryDefectClaimRequiredCodeRule("primary defect claim needs an entry"),
        ),
    )
}
