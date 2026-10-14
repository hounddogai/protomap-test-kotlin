package com.acme.transport.customs.hold.profile

import com.acme.transport.kit.text.normalize

/** A rule a adaptive hold target must pass. Check returns its problems, or none. */
interface AdaptiveHoldTargetRule {
    fun check(item: AdaptiveHoldTarget): MutableList<String>
}

data class AdaptiveHoldTargetRequiredNameRule(var message: String) : AdaptiveHoldTargetRule {
    override fun check(item: AdaptiveHoldTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldTargetAllowedChannelRule(var allowed: MutableList<String>) : AdaptiveHoldTargetRule {
    override fun check(item: AdaptiveHoldTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveHoldTargetAttemptsRangeRule(var min: Int, var max: Int) : AdaptiveHoldTargetRule {
    override fun check(item: AdaptiveHoldTarget): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class AdaptiveHoldTargetCodePrefixRule(var prefix: String) : AdaptiveHoldTargetRule {
    override fun check(item: AdaptiveHoldTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive hold target. */
data class AdaptiveHoldTargetValidator(var rules: MutableList<AdaptiveHoldTargetRule>) {
    fun validate(item: AdaptiveHoldTarget): MutableList<String> {
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

    fun requireValid(item: AdaptiveHoldTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveHoldTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveHoldTargetValidator(): AdaptiveHoldTargetValidator {
    return AdaptiveHoldTargetValidator(
        mutableListOf<AdaptiveHoldTargetRule>(
            AdaptiveHoldTargetRequiredNameRule("adaptive hold target needs a value"),
            AdaptiveHoldTargetAllowedChannelRule(mutableListOf<String>("tiered")),
            AdaptiveHoldTargetAttemptsRangeRule(64, 120),
            AdaptiveHoldTargetCodePrefixRule("OPS"),
        ),
    )
}
