package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.normalize

/** A rule a adaptive sortation bundle must pass. Check returns its problems, or none. */
interface AdaptiveSortationBundleRule {
    fun check(item: AdaptiveSortationBundle): MutableList<String>
}

data class AdaptiveSortationBundleExternalRefLengthRule(var maxLength: Int) : AdaptiveSortationBundleRule {
    override fun check(item: AdaptiveSortationBundle): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationBundleAllowedChannelRule(
    var allowed: MutableList<String>,
) : AdaptiveSortationBundleRule {
    override fun check(item: AdaptiveSortationBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationBundleCodePrefixRule(var prefix: String) : AdaptiveSortationBundleRule {
    override fun check(item: AdaptiveSortationBundle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationBundleRequiredCodeRule(var message: String) : AdaptiveSortationBundleRule {
    override fun check(item: AdaptiveSortationBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationBundleSequenceRangeRule(var min: Int, var max: Int) : AdaptiveSortationBundleRule {
    override fun check(item: AdaptiveSortationBundle): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class AdaptiveSortationBundleGraceDaysRangeRule(var min: Int, var max: Int) : AdaptiveSortationBundleRule {
    override fun check(item: AdaptiveSortationBundle): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive sortation bundle. */
data class AdaptiveSortationBundleValidator(var rules: MutableList<AdaptiveSortationBundleRule>) {
    fun validate(item: AdaptiveSortationBundle): MutableList<String> {
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

    fun requireValid(item: AdaptiveSortationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSortationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSortationBundleValidator(): AdaptiveSortationBundleValidator {
    return AdaptiveSortationBundleValidator(
        mutableListOf<AdaptiveSortationBundleRule>(
            AdaptiveSortationBundleExternalRefLengthRule(80),
            AdaptiveSortationBundleAllowedChannelRule(mutableListOf<String>()),
            AdaptiveSortationBundleCodePrefixRule("X"),
            AdaptiveSortationBundleRequiredCodeRule("adaptive sortation bundle needs this field"),
            AdaptiveSortationBundleSequenceRangeRule(16, 86400),
            AdaptiveSortationBundleGraceDaysRangeRule(64, 1000),
        ),
    )
}
