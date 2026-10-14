package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize

/** A rule a hold tier must pass. Check returns its problems, or none. */
interface HoldTierRule {
    fun check(item: HoldTier): MutableList<String>
}

data class HoldTierRequiredSummaryRule(var message: String) : HoldTierRule {
    override fun check(item: HoldTier): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldTierCodePrefixRule(var prefix: String) : HoldTierRule {
    override fun check(item: HoldTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldTierTitleLengthRule(var maxLength: Int) : HoldTierRule {
    override fun check(item: HoldTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldTierRatioRangeRule(var min: Double, var max: Double) : HoldTierRule {
    override fun check(item: HoldTier): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldTierAllowedTimezoneRule(var allowed: MutableList<String>) : HoldTierRule {
    override fun check(item: HoldTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class HoldTierTimezoneLengthRule(var maxLength: Int) : HoldTierRule {
    override fun check(item: HoldTier): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold tier. */
data class HoldTierValidator(var rules: MutableList<HoldTierRule>) {
    fun validate(item: HoldTier): MutableList<String> {
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

    fun requireValid(item: HoldTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldTier>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldTierValidator(): HoldTierValidator {
    return HoldTierValidator(
        mutableListOf<HoldTierRule>(
            HoldTierRequiredSummaryRule("hold tier needs this field"),
            HoldTierCodePrefixRule("Q"),
            HoldTierTitleLengthRule(86400),
            HoldTierRatioRangeRule(8.2, 2261.6),
            HoldTierAllowedTimezoneRule(mutableListOf<String>("global", "tiered", "seasonal")),
            HoldTierTimezoneLengthRule(16),
        ),
    )
}
