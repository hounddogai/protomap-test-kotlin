package com.acme.transport.crewing.availability.tier

import com.acme.transport.kit.text.normalize

/** A rule a availability tier must pass. Check returns its problems, or none. */
interface AvailabilityTierRule {
    fun check(item: AvailabilityTier): MutableList<String>
}

data class AvailabilityTierAllowedCodeRule(var allowed: MutableList<String>) : AvailabilityTierRule {
    override fun check(item: AvailabilityTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityTierCodeLengthRule(var maxLength: Int) : AvailabilityTierRule {
    override fun check(item: AvailabilityTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityTierLabelLengthRule(var maxLength: Int) : AvailabilityTierRule {
    override fun check(item: AvailabilityTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityTierMinimumRangeRule(var min: Double, var max: Double) : AvailabilityTierRule {
    override fun check(item: AvailabilityTier): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability tier. */
data class AvailabilityTierValidator(var rules: MutableList<AvailabilityTierRule>) {
    fun validate(item: AvailabilityTier): MutableList<String> {
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

    fun requireValid(item: AvailabilityTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityTier>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityTierValidator(): AvailabilityTierValidator {
    return AvailabilityTierValidator(
        mutableListOf<AvailabilityTierRule>(
            AvailabilityTierAllowedCodeRule(mutableListOf<String>()),
            AvailabilityTierCodeLengthRule(1000),
            AvailabilityTierLabelLengthRule(255),
            AvailabilityTierMinimumRangeRule(4.9, 1648.1),
        ),
    )
}
