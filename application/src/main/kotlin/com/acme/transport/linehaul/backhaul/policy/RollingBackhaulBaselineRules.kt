package com.acme.transport.linehaul.backhaul.policy

import com.acme.transport.kit.text.normalize

/** A rule a rolling backhaul baseline must pass. Check returns its problems, or none. */
interface RollingBackhaulBaselineRule {
    fun check(item: RollingBackhaulBaseline): MutableList<String>
}

data class RollingBackhaulBaselineRequiredCodeRule(var message: String) : RollingBackhaulBaselineRule {
    override fun check(item: RollingBackhaulBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulBaselineCodeLengthRule(var maxLength: Int) : RollingBackhaulBaselineRule {
    override fun check(item: RollingBackhaulBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingBackhaulBaselineUpdatedAtRangeRule(var min: Int, var max: Int) : RollingBackhaulBaselineRule {
    override fun check(item: RollingBackhaulBaseline): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class RollingBackhaulBaselineFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : RollingBackhaulBaselineRule {
    override fun check(item: RollingBackhaulBaseline): MutableList<String> {
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

data class RollingBackhaulBaselineAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingBackhaulBaselineRule {
    override fun check(item: RollingBackhaulBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling backhaul baseline. */
data class RollingBackhaulBaselineValidator(var rules: MutableList<RollingBackhaulBaselineRule>) {
    fun validate(item: RollingBackhaulBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingBackhaulBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingBackhaulBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingBackhaulBaselineValidator(): RollingBackhaulBaselineValidator {
    return RollingBackhaulBaselineValidator(
        mutableListOf<RollingBackhaulBaselineRule>(
            RollingBackhaulBaselineRequiredCodeRule("rolling backhaul baseline needs an entry"),
            RollingBackhaulBaselineCodeLengthRule(64),
            RollingBackhaulBaselineUpdatedAtRangeRule(16, 86400),
            RollingBackhaulBaselineFuelLitersRangeRule(8.4, 4662.6),
            RollingBackhaulBaselineAllowedCodeRule(mutableListOf<String>("fallback", "batched")),
        ),
    )
}
