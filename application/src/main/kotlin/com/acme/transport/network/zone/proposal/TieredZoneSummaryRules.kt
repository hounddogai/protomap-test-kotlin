package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.text.normalize

/** A rule a tiered zone summary must pass. Check returns its problems, or none. */
interface TieredZoneSummaryRule {
    fun check(item: TieredZoneSummary): MutableList<String>
}

data class TieredZoneSummaryCodeLengthRule(var maxLength: Int) : TieredZoneSummaryRule {
    override fun check(item: TieredZoneSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredZoneSummaryAllowedCodeRule(var allowed: MutableList<String>) : TieredZoneSummaryRule {
    override fun check(item: TieredZoneSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class TieredZoneSummaryCostRangeRule(var min: Double, var max: Double) : TieredZoneSummaryRule {
    override fun check(item: TieredZoneSummary): MutableList<String> {
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

/** Runs every rule of a tiered zone summary. */
data class TieredZoneSummaryValidator(var rules: MutableList<TieredZoneSummaryRule>) {
    fun validate(item: TieredZoneSummary): MutableList<String> {
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

    fun requireValid(item: TieredZoneSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredZoneSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredZoneSummaryValidator(): TieredZoneSummaryValidator {
    return TieredZoneSummaryValidator(
        mutableListOf<TieredZoneSummaryRule>(
            TieredZoneSummaryCodeLengthRule(80),
            TieredZoneSummaryAllowedCodeRule(mutableListOf<String>("manual", "regional")),
            TieredZoneSummaryCostRangeRule(1.3, 3116.4),
        ),
    )
}
