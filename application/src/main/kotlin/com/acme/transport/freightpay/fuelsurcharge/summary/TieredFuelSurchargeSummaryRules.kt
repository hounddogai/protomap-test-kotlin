package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.kit.text.normalize

/** A rule a tiered fuel surcharge summary must pass. Check returns its problems, or none. */
interface TieredFuelSurchargeSummaryRule {
    fun check(item: TieredFuelSurchargeSummary): MutableList<String>
}

data class TieredFuelSurchargeSummaryCodeLengthRule(var maxLength: Int) : TieredFuelSurchargeSummaryRule {
    override fun check(item: TieredFuelSurchargeSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeSummaryRequiredTitleRule(var message: String) : TieredFuelSurchargeSummaryRule {
    override fun check(item: TieredFuelSurchargeSummary): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeSummaryStopCountRangeRule(
    var min: Int,
    var max: Int,
) : TieredFuelSurchargeSummaryRule {
    override fun check(item: TieredFuelSurchargeSummary): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

data class TieredFuelSurchargeSummaryPinnedGuardRule(var limit: Int) : TieredFuelSurchargeSummaryRule {
    override fun check(item: TieredFuelSurchargeSummary): MutableList<String> {
        if (item.pinned && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeSummaryCodePrefixRule(var prefix: String) : TieredFuelSurchargeSummaryRule {
    override fun check(item: TieredFuelSurchargeSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredFuelSurchargeSummaryAllowedTitleRule(
    var allowed: MutableList<String>,
) : TieredFuelSurchargeSummaryRule {
    override fun check(item: TieredFuelSurchargeSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered fuel surcharge summary. */
data class TieredFuelSurchargeSummaryValidator(var rules: MutableList<TieredFuelSurchargeSummaryRule>) {
    fun validate(item: TieredFuelSurchargeSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredFuelSurchargeSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredFuelSurchargeSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredFuelSurchargeSummaryValidator(): TieredFuelSurchargeSummaryValidator {
    return TieredFuelSurchargeSummaryValidator(
        mutableListOf<TieredFuelSurchargeSummaryRule>(
            TieredFuelSurchargeSummaryCodeLengthRule(3600),
            TieredFuelSurchargeSummaryRequiredTitleRule("tiered fuel surcharge summary needs this field"),
            TieredFuelSurchargeSummaryStopCountRangeRule(80, 80),
            TieredFuelSurchargeSummaryPinnedGuardRule(86400),
            TieredFuelSurchargeSummaryCodePrefixRule("OPS"),
            TieredFuelSurchargeSummaryAllowedTitleRule(mutableListOf<String>("pending", "pending", "scheduled")),
        ),
    )
}
