package com.acme.transport.fleetops.utilization.group

import com.acme.transport.kit.text.normalize

/** A rule a manual utilization claim must pass. Check returns its problems, or none. */
interface ManualUtilizationClaimRule {
    fun check(item: ManualUtilizationClaim): MutableList<String>
}

data class ManualUtilizationClaimCreatedAtRangeRule(var min: Int, var max: Int) : ManualUtilizationClaimRule {
    override fun check(item: ManualUtilizationClaim): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationClaimAllowedChannelRule(
    var allowed: MutableList<String>,
) : ManualUtilizationClaimRule {
    override fun check(item: ManualUtilizationClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationClaimRequiredLabelRule(var message: String) : ManualUtilizationClaimRule {
    override fun check(item: ManualUtilizationClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual utilization claim. */
data class ManualUtilizationClaimValidator(var rules: MutableList<ManualUtilizationClaimRule>) {
    fun validate(item: ManualUtilizationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualUtilizationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualUtilizationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultManualUtilizationClaimValidator(): ManualUtilizationClaimValidator {
    return ManualUtilizationClaimValidator(
        mutableListOf<ManualUtilizationClaimRule>(
            ManualUtilizationClaimCreatedAtRangeRule(255, 255),
            ManualUtilizationClaimAllowedChannelRule(mutableListOf<String>("capped")),
            ManualUtilizationClaimRequiredLabelRule("manual utilization claim needs this field"),
        ),
    )
}
