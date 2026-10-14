package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.normalize

/** A rule a primary utilization target must pass. Check returns its problems, or none. */
interface PrimaryUtilizationTargetRule {
    fun check(item: PrimaryUtilizationTarget): MutableList<String>
}

data class PrimaryUtilizationTargetCodePrefixRule(var prefix: String) : PrimaryUtilizationTargetRule {
    override fun check(item: PrimaryUtilizationTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationTargetAllowedCodeRule(
    var allowed: MutableList<String>,
) : PrimaryUtilizationTargetRule {
    override fun check(item: PrimaryUtilizationTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationTargetRequiredLabelRule(var message: String) : PrimaryUtilizationTargetRule {
    override fun check(item: PrimaryUtilizationTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationTargetCreatedAtRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryUtilizationTargetRule {
    override fun check(item: PrimaryUtilizationTarget): MutableList<String> {
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

data class PrimaryUtilizationTargetRequiredCodeRule(var message: String) : PrimaryUtilizationTargetRule {
    override fun check(item: PrimaryUtilizationTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryUtilizationTargetRequiredOwnerIdRule(var message: String) : PrimaryUtilizationTargetRule {
    override fun check(item: PrimaryUtilizationTarget): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary utilization target. */
data class PrimaryUtilizationTargetValidator(var rules: MutableList<PrimaryUtilizationTargetRule>) {
    fun validate(item: PrimaryUtilizationTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryUtilizationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryUtilizationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryUtilizationTargetValidator(): PrimaryUtilizationTargetValidator {
    return PrimaryUtilizationTargetValidator(
        mutableListOf<PrimaryUtilizationTargetRule>(
            PrimaryUtilizationTargetCodePrefixRule("Q"),
            PrimaryUtilizationTargetAllowedCodeRule(mutableListOf<String>("strict")),
            PrimaryUtilizationTargetRequiredLabelRule("primary utilization target needs a value"),
            PrimaryUtilizationTargetCreatedAtRangeRule(80, 86400),
            PrimaryUtilizationTargetRequiredCodeRule("primary utilization target needs a value"),
            PrimaryUtilizationTargetRequiredOwnerIdRule("primary utilization target needs an entry"),
        ),
    )
}
