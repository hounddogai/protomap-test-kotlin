package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.normalize

/** A rule a strict trailer policy must pass. Check returns its problems, or none. */
interface StrictTrailerPolicyRule {
    fun check(item: StrictTrailerPolicy): MutableList<String>
}

data class StrictTrailerPolicyRequiredTitleRule(var message: String) : StrictTrailerPolicyRule {
    override fun check(item: StrictTrailerPolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPolicyTitleLengthRule(var maxLength: Int) : StrictTrailerPolicyRule {
    override fun check(item: StrictTrailerPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPolicyCodePrefixRule(var prefix: String) : StrictTrailerPolicyRule {
    override fun check(item: StrictTrailerPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPolicyScoreRangeRule(var min: Double, var max: Double) : StrictTrailerPolicyRule {
    override fun check(item: StrictTrailerPolicy): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPolicyRequiredChannelRule(var message: String) : StrictTrailerPolicyRule {
    override fun check(item: StrictTrailerPolicy): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTrailerPolicyAllowedTitleRule(var allowed: MutableList<String>) : StrictTrailerPolicyRule {
    override fun check(item: StrictTrailerPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict trailer policy. */
data class StrictTrailerPolicyValidator(var rules: MutableList<StrictTrailerPolicyRule>) {
    fun validate(item: StrictTrailerPolicy): MutableList<String> {
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

    fun requireValid(item: StrictTrailerPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTrailerPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTrailerPolicyValidator(): StrictTrailerPolicyValidator {
    return StrictTrailerPolicyValidator(
        mutableListOf<StrictTrailerPolicyRule>(
            StrictTrailerPolicyRequiredTitleRule("strict trailer policy needs an entry"),
            StrictTrailerPolicyTitleLengthRule(64),
            StrictTrailerPolicyCodePrefixRule("X"),
            StrictTrailerPolicyScoreRangeRule(1.4, 1169.2),
            StrictTrailerPolicyRequiredChannelRule("strict trailer policy needs an entry"),
            StrictTrailerPolicyAllowedTitleRule(mutableListOf<String>("rolling", "rolling")),
        ),
    )
}
