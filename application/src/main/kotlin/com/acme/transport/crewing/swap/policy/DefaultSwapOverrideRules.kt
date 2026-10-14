package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.text.normalize

/** A rule a default swap override must pass. Check returns its problems, or none. */
interface DefaultSwapOverrideRule {
    fun check(item: DefaultSwapOverride): MutableList<String>
}

data class DefaultSwapOverrideCodeLengthRule(var maxLength: Int) : DefaultSwapOverrideRule {
    override fun check(item: DefaultSwapOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapOverrideAllowedTitleRule(var allowed: MutableList<String>) : DefaultSwapOverrideRule {
    override fun check(item: DefaultSwapOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapOverrideRequiredCodeRule(var message: String) : DefaultSwapOverrideRule {
    override fun check(item: DefaultSwapOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapOverrideRequiredTrailerIdRule(var message: String) : DefaultSwapOverrideRule {
    override fun check(item: DefaultSwapOverride): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapOverrideTrailerIdLengthRule(var maxLength: Int) : DefaultSwapOverrideRule {
    override fun check(item: DefaultSwapOverride): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultSwapOverrideUpdatedAtRangeRule(var min: Int, var max: Int) : DefaultSwapOverrideRule {
    override fun check(item: DefaultSwapOverride): MutableList<String> {
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

/** Runs every rule of a default swap override. */
data class DefaultSwapOverrideValidator(var rules: MutableList<DefaultSwapOverrideRule>) {
    fun validate(item: DefaultSwapOverride): MutableList<String> {
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

    fun requireValid(item: DefaultSwapOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultSwapOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultSwapOverrideValidator(): DefaultSwapOverrideValidator {
    return DefaultSwapOverrideValidator(
        mutableListOf<DefaultSwapOverrideRule>(
            DefaultSwapOverrideCodeLengthRule(120),
            DefaultSwapOverrideAllowedTitleRule(mutableListOf<String>("strict")),
            DefaultSwapOverrideRequiredCodeRule("default swap override needs an entry"),
            DefaultSwapOverrideRequiredTrailerIdRule("default swap override needs this field"),
            DefaultSwapOverrideTrailerIdLengthRule(1000),
            DefaultSwapOverrideUpdatedAtRangeRule(120, 3600),
        ),
    )
}
