package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize

/** A rule a adaptive swap window must pass. Check returns its problems, or none. */
interface AdaptiveSwapWindowRule {
    fun check(item: AdaptiveSwapWindow): MutableList<String>
}

data class AdaptiveSwapWindowTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : AdaptiveSwapWindowRule {
    override fun check(item: AdaptiveSwapWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class AdaptiveSwapWindowLabelLengthRule(var maxLength: Int) : AdaptiveSwapWindowRule {
    override fun check(item: AdaptiveSwapWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapWindowAllowedLabelRule(var allowed: MutableList<String>) : AdaptiveSwapWindowRule {
    override fun check(item: AdaptiveSwapWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSwapWindowCodeLengthRule(var maxLength: Int) : AdaptiveSwapWindowRule {
    override fun check(item: AdaptiveSwapWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive swap window. */
data class AdaptiveSwapWindowValidator(var rules: MutableList<AdaptiveSwapWindowRule>) {
    fun validate(item: AdaptiveSwapWindow): MutableList<String> {
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

    fun requireValid(item: AdaptiveSwapWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSwapWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSwapWindowValidator(): AdaptiveSwapWindowValidator {
    return AdaptiveSwapWindowValidator(
        mutableListOf<AdaptiveSwapWindowRule>(
            AdaptiveSwapWindowTagsLimitRule(120, mutableListOf<String>()),
            AdaptiveSwapWindowLabelLengthRule(16),
            AdaptiveSwapWindowAllowedLabelRule(mutableListOf<String>("seasonal", "default")),
            AdaptiveSwapWindowCodeLengthRule(32),
        ),
    )
}
