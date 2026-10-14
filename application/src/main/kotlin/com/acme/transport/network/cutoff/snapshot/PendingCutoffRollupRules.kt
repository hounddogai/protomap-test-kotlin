package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a pending cutoff rollup must pass. Check returns its problems, or none. */
interface PendingCutoffRollupRule {
    fun check(item: PendingCutoffRollup): MutableList<String>
}

data class PendingCutoffRollupAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : PendingCutoffRollupRule {
    override fun check(item: PendingCutoffRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffRollupExpiresAtRangeRule(var min: Int, var max: Int) : PendingCutoffRollupRule {
    override fun check(item: PendingCutoffRollup): MutableList<String> {
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

data class PendingCutoffRollupCapacityRangeRule(var min: Int, var max: Int) : PendingCutoffRollupRule {
    override fun check(item: PendingCutoffRollup): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cutoff rollup. */
data class PendingCutoffRollupValidator(var rules: MutableList<PendingCutoffRollupRule>) {
    fun validate(item: PendingCutoffRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingCutoffRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCutoffRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCutoffRollupValidator(): PendingCutoffRollupValidator {
    return PendingCutoffRollupValidator(
        mutableListOf<PendingCutoffRollupRule>(
            PendingCutoffRollupAllowedContactEmailRule(mutableListOf<String>()),
            PendingCutoffRollupExpiresAtRangeRule(16, 32),
            PendingCutoffRollupCapacityRangeRule(16, 3600),
        ),
    )
}
