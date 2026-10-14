package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize

/** A rule a default dock door tier must pass. Check returns its problems, or none. */
interface DefaultDockDoorTierRule {
    fun check(item: DefaultDockDoorTier): MutableList<String>
}

data class DefaultDockDoorTierCodePrefixRule(var prefix: String) : DefaultDockDoorTierRule {
    override fun check(item: DefaultDockDoorTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorTierNameLengthRule(var maxLength: Int) : DefaultDockDoorTierRule {
    override fun check(item: DefaultDockDoorTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorTierSequenceRangeRule(var min: Int, var max: Int) : DefaultDockDoorTierRule {
    override fun check(item: DefaultDockDoorTier): MutableList<String> {
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

data class DefaultDockDoorTierCodeLengthRule(var maxLength: Int) : DefaultDockDoorTierRule {
    override fun check(item: DefaultDockDoorTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDockDoorTierGraceDaysRangeRule(var min: Int, var max: Int) : DefaultDockDoorTierRule {
    override fun check(item: DefaultDockDoorTier): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class DefaultDockDoorTierAllowedCodeRule(var allowed: MutableList<String>) : DefaultDockDoorTierRule {
    override fun check(item: DefaultDockDoorTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default dock door tier. */
data class DefaultDockDoorTierValidator(var rules: MutableList<DefaultDockDoorTierRule>) {
    fun validate(item: DefaultDockDoorTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDockDoorTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDockDoorTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDockDoorTierValidator(): DefaultDockDoorTierValidator {
    return DefaultDockDoorTierValidator(
        mutableListOf<DefaultDockDoorTierRule>(
            DefaultDockDoorTierCodePrefixRule("OPS"),
            DefaultDockDoorTierNameLengthRule(120),
            DefaultDockDoorTierSequenceRangeRule(16, 120),
            DefaultDockDoorTierCodeLengthRule(255),
            DefaultDockDoorTierGraceDaysRangeRule(16, 32),
            DefaultDockDoorTierAllowedCodeRule(mutableListOf<String>("capped", "tiered")),
        ),
    )
}
