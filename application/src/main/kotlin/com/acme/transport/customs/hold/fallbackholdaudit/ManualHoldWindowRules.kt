package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.normalize

/** A rule a manual hold window must pass. Check returns its problems, or none. */
interface ManualHoldWindowRule {
    fun check(item: ManualHoldWindow): MutableList<String>
}

data class ManualHoldWindowStartsAtRangeRule(var min: Int, var max: Int) : ManualHoldWindowRule {
    override fun check(item: ManualHoldWindow): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class ManualHoldWindowCodeLengthRule(var maxLength: Int) : ManualHoldWindowRule {
    override fun check(item: ManualHoldWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldWindowRequiredChannelRule(var message: String) : ManualHoldWindowRule {
    override fun check(item: ManualHoldWindow): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldWindowCodePrefixRule(var prefix: String) : ManualHoldWindowRule {
    override fun check(item: ManualHoldWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldWindowNameLengthRule(var maxLength: Int) : ManualHoldWindowRule {
    override fun check(item: ManualHoldWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldWindowAllowedCountryRule(var allowed: MutableList<String>) : ManualHoldWindowRule {
    override fun check(item: ManualHoldWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual hold window. */
data class ManualHoldWindowValidator(var rules: MutableList<ManualHoldWindowRule>) {
    fun validate(item: ManualHoldWindow): MutableList<String> {
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

    fun requireValid(item: ManualHoldWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHoldWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHoldWindowValidator(): ManualHoldWindowValidator {
    return ManualHoldWindowValidator(
        mutableListOf<ManualHoldWindowRule>(
            ManualHoldWindowStartsAtRangeRule(64, 120),
            ManualHoldWindowCodeLengthRule(80),
            ManualHoldWindowRequiredChannelRule("manual hold window needs this field"),
            ManualHoldWindowCodePrefixRule("REF"),
            ManualHoldWindowNameLengthRule(64),
            ManualHoldWindowAllowedCountryRule(mutableListOf<String>("manual", "regional", "strict")),
        ),
    )
}
