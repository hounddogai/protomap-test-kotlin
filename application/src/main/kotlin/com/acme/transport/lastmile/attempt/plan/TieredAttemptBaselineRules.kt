package com.acme.transport.lastmile.attempt.plan

import com.acme.transport.kit.text.normalize

/** A rule a tiered attempt baseline must pass. Check returns its problems, or none. */
interface TieredAttemptBaselineRule {
    fun check(item: TieredAttemptBaseline): MutableList<String>
}

data class TieredAttemptBaselineCodeLengthRule(var maxLength: Int) : TieredAttemptBaselineRule {
    override fun check(item: TieredAttemptBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptBaselineRequiredCarrierRule(var message: String) : TieredAttemptBaselineRule {
    override fun check(item: TieredAttemptBaseline): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptBaselineRequiredLaneRule(var message: String) : TieredAttemptBaselineRule {
    override fun check(item: TieredAttemptBaseline): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptBaselineAllowedLabelRule(var allowed: MutableList<String>) : TieredAttemptBaselineRule {
    override fun check(item: TieredAttemptBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptBaselineRequiredCodeRule(var message: String) : TieredAttemptBaselineRule {
    override fun check(item: TieredAttemptBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredAttemptBaselineLaneLengthRule(var maxLength: Int) : TieredAttemptBaselineRule {
    override fun check(item: TieredAttemptBaseline): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered attempt baseline. */
data class TieredAttemptBaselineValidator(var rules: MutableList<TieredAttemptBaselineRule>) {
    fun validate(item: TieredAttemptBaseline): MutableList<String> {
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

    fun requireValid(item: TieredAttemptBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredAttemptBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredAttemptBaselineValidator(): TieredAttemptBaselineValidator {
    return TieredAttemptBaselineValidator(
        mutableListOf<TieredAttemptBaselineRule>(
            TieredAttemptBaselineCodeLengthRule(86400),
            TieredAttemptBaselineRequiredCarrierRule("tiered attempt baseline needs a value"),
            TieredAttemptBaselineRequiredLaneRule("tiered attempt baseline needs a value"),
            TieredAttemptBaselineAllowedLabelRule(mutableListOf<String>("weighted", "primary", "batched")),
            TieredAttemptBaselineRequiredCodeRule("tiered attempt baseline needs an entry"),
            TieredAttemptBaselineLaneLengthRule(16),
        ),
    )
}
