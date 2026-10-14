package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.normalize

/** A rule a pending near miss threshold must pass. Check returns its problems, or none. */
interface PendingNearMissThresholdRule {
    fun check(item: PendingNearMissThreshold): MutableList<String>
}

data class PendingNearMissThresholdAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingNearMissThresholdRule {
    override fun check(item: PendingNearMissThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissThresholdNameLengthRule(var maxLength: Int) : PendingNearMissThresholdRule {
    override fun check(item: PendingNearMissThreshold): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNearMissThresholdCostRangeRule(
    var min: Double,
    var max: Double,
) : PendingNearMissThresholdRule {
    override fun check(item: PendingNearMissThreshold): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending near miss threshold. */
data class PendingNearMissThresholdValidator(var rules: MutableList<PendingNearMissThresholdRule>) {
    fun validate(item: PendingNearMissThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingNearMissThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNearMissThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNearMissThresholdValidator(): PendingNearMissThresholdValidator {
    return PendingNearMissThresholdValidator(
        mutableListOf<PendingNearMissThresholdRule>(
            PendingNearMissThresholdAllowedCodeRule(mutableListOf<String>("weighted", "pending")),
            PendingNearMissThresholdNameLengthRule(16),
            PendingNearMissThresholdCostRangeRule(1.2, 4579.5),
        ),
    )
}
