package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.text.normalize

/** A rule a pending rest break rollup must pass. Check returns its problems, or none. */
interface PendingRestBreakRollupRule {
    fun check(item: PendingRestBreakRollup): MutableList<String>
}

data class PendingRestBreakRollupAllowedLocaleRule(
    var allowed: MutableList<String>,
) : PendingRestBreakRollupRule {
    override fun check(item: PendingRestBreakRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakRollupCarrierLengthRule(var maxLength: Int) : PendingRestBreakRollupRule {
    override fun check(item: PendingRestBreakRollup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRestBreakRollupWeightRangeRule(
    var min: Double,
    var max: Double,
) : PendingRestBreakRollupRule {
    override fun check(item: PendingRestBreakRollup): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending rest break rollup. */
data class PendingRestBreakRollupValidator(var rules: MutableList<PendingRestBreakRollupRule>) {
    fun validate(item: PendingRestBreakRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRestBreakRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRestBreakRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRestBreakRollupValidator(): PendingRestBreakRollupValidator {
    return PendingRestBreakRollupValidator(
        mutableListOf<PendingRestBreakRollupRule>(
            PendingRestBreakRollupAllowedLocaleRule(mutableListOf<String>()),
            PendingRestBreakRollupCarrierLengthRule(255),
            PendingRestBreakRollupWeightRangeRule(0.7, 1808.4),
        ),
    )
}
