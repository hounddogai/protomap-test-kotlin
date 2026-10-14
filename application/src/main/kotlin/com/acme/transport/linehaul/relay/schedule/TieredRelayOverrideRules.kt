package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.text.normalize

/** A rule a tiered relay override must pass. Check returns its problems, or none. */
interface TieredRelayOverrideRule {
    fun check(item: TieredRelayOverride): MutableList<String>
}

data class TieredRelayOverrideAllowedCodeRule(var allowed: MutableList<String>) : TieredRelayOverrideRule {
    override fun check(item: TieredRelayOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayOverrideWindowDaysRangeRule(var min: Int, var max: Int) : TieredRelayOverrideRule {
    override fun check(item: TieredRelayOverride): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class TieredRelayOverrideCeilingRangeRule(var min: Double, var max: Double) : TieredRelayOverrideRule {
    override fun check(item: TieredRelayOverride): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayOverrideCodePrefixRule(var prefix: String) : TieredRelayOverrideRule {
    override fun check(item: TieredRelayOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayOverrideCodeLengthRule(var maxLength: Int) : TieredRelayOverrideRule {
    override fun check(item: TieredRelayOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayOverrideTitleLengthRule(var maxLength: Int) : TieredRelayOverrideRule {
    override fun check(item: TieredRelayOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered relay override. */
data class TieredRelayOverrideValidator(var rules: MutableList<TieredRelayOverrideRule>) {
    fun validate(item: TieredRelayOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRelayOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRelayOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRelayOverrideValidator(): TieredRelayOverrideValidator {
    return TieredRelayOverrideValidator(
        mutableListOf<TieredRelayOverrideRule>(
            TieredRelayOverrideAllowedCodeRule(mutableListOf<String>()),
            TieredRelayOverrideWindowDaysRangeRule(64, 255),
            TieredRelayOverrideCeilingRangeRule(1.4, 4001.5),
            TieredRelayOverrideCodePrefixRule("Q"),
            TieredRelayOverrideCodeLengthRule(1000),
            TieredRelayOverrideTitleLengthRule(32),
        ),
    )
}
