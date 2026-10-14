package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.text.normalize

/** A rule a adaptive capacity entry must pass. Check returns its problems, or none. */
interface AdaptiveCapacityEntryRule {
    fun check(item: AdaptiveCapacityEntry): MutableList<String>
}

data class AdaptiveCapacityEntryAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : AdaptiveCapacityEntryRule {
    override fun check(item: AdaptiveCapacityEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityEntryPhoneLengthRule(var maxLength: Int) : AdaptiveCapacityEntryRule {
    override fun check(item: AdaptiveCapacityEntry): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityEntryMinimumRangeRule(var min: Double, var max: Double) : AdaptiveCapacityEntryRule {
    override fun check(item: AdaptiveCapacityEntry): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class AdaptiveCapacityEntryActiveGuardRule(var limit: Double) : AdaptiveCapacityEntryRule {
    override fun check(item: AdaptiveCapacityEntry): MutableList<String> {
        if (item.active && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityEntryRequiredPhoneRule(var message: String) : AdaptiveCapacityEntryRule {
    override fun check(item: AdaptiveCapacityEntry): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCapacityEntryAmountRangeRule(var min: Double, var max: Double) : AdaptiveCapacityEntryRule {
    override fun check(item: AdaptiveCapacityEntry): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive capacity entry. */
data class AdaptiveCapacityEntryValidator(var rules: MutableList<AdaptiveCapacityEntryRule>) {
    fun validate(item: AdaptiveCapacityEntry): MutableList<String> {
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

    fun requireValid(item: AdaptiveCapacityEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCapacityEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCapacityEntryValidator(): AdaptiveCapacityEntryValidator {
    return AdaptiveCapacityEntryValidator(
        mutableListOf<AdaptiveCapacityEntryRule>(
            AdaptiveCapacityEntryAllowedContactEmailRule(
                mutableListOf<String>("strict", "seasonal", "fallback"),
            ),
            AdaptiveCapacityEntryPhoneLengthRule(64),
            AdaptiveCapacityEntryMinimumRangeRule(8.9, 3780.3),
            AdaptiveCapacityEntryActiveGuardRule(9.9),
            AdaptiveCapacityEntryRequiredPhoneRule("adaptive capacity entry needs an entry"),
            AdaptiveCapacityEntryAmountRangeRule(1.6, 2140.6),
        ),
    )
}
