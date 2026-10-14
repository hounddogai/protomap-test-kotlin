package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.normalize

/** A rule a adaptive hold template must pass. Check returns its problems, or none. */
interface AdaptiveHoldTemplateRule {
    fun check(item: AdaptiveHoldTemplate): MutableList<String>
}

data class AdaptiveHoldTemplateRequiredTitleRule(var message: String) : AdaptiveHoldTemplateRule {
    override fun check(item: AdaptiveHoldTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldTemplateAllowedCodeRule(var allowed: MutableList<String>) : AdaptiveHoldTemplateRule {
    override fun check(item: AdaptiveHoldTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldTemplateRequiredTrailerIdRule(var message: String) : AdaptiveHoldTemplateRule {
    override fun check(item: AdaptiveHoldTemplate): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldTemplateCodePrefixRule(var prefix: String) : AdaptiveHoldTemplateRule {
    override fun check(item: AdaptiveHoldTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldTemplateRequiredCodeRule(var message: String) : AdaptiveHoldTemplateRule {
    override fun check(item: AdaptiveHoldTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldTemplateTitleLengthRule(var maxLength: Int) : AdaptiveHoldTemplateRule {
    override fun check(item: AdaptiveHoldTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive hold template. */
data class AdaptiveHoldTemplateValidator(var rules: MutableList<AdaptiveHoldTemplateRule>) {
    fun validate(item: AdaptiveHoldTemplate): MutableList<String> {
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

    fun requireValid(item: AdaptiveHoldTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveHoldTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveHoldTemplateValidator(): AdaptiveHoldTemplateValidator {
    return AdaptiveHoldTemplateValidator(
        mutableListOf<AdaptiveHoldTemplateRule>(
            AdaptiveHoldTemplateRequiredTitleRule("adaptive hold template needs a value"),
            AdaptiveHoldTemplateAllowedCodeRule(mutableListOf<String>("pending", "global")),
            AdaptiveHoldTemplateRequiredTrailerIdRule("adaptive hold template needs an entry"),
            AdaptiveHoldTemplateCodePrefixRule("Q"),
            AdaptiveHoldTemplateRequiredCodeRule("adaptive hold template needs a value"),
            AdaptiveHoldTemplateTitleLengthRule(32),
        ),
    )
}
