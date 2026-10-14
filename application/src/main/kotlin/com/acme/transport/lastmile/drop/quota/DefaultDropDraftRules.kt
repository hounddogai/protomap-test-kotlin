package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.normalize

/** A rule a default drop draft must pass. Check returns its problems, or none. */
interface DefaultDropDraftRule {
    fun check(item: DefaultDropDraft): MutableList<String>
}

data class DefaultDropDraftRequiredCarrierRule(var message: String) : DefaultDropDraftRule {
    override fun check(item: DefaultDropDraft): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropDraftAllowedTrailerIdRule(var allowed: MutableList<String>) : DefaultDropDraftRule {
    override fun check(item: DefaultDropDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is expired")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropDraftDiscountRangeRule(var min: Double, var max: Double) : DefaultDropDraftRule {
    override fun check(item: DefaultDropDraft): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class DefaultDropDraftCodePrefixRule(var prefix: String) : DefaultDropDraftRule {
    override fun check(item: DefaultDropDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropDraftCeilingRangeRule(var min: Double, var max: Double) : DefaultDropDraftRule {
    override fun check(item: DefaultDropDraft): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropDraftTrailerIdLengthRule(var maxLength: Int) : DefaultDropDraftRule {
    override fun check(item: DefaultDropDraft): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default drop draft. */
data class DefaultDropDraftValidator(var rules: MutableList<DefaultDropDraftRule>) {
    fun validate(item: DefaultDropDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDropDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDropDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDropDraftValidator(): DefaultDropDraftValidator {
    return DefaultDropDraftValidator(
        mutableListOf<DefaultDropDraftRule>(
            DefaultDropDraftRequiredCarrierRule("default drop draft needs a value"),
            DefaultDropDraftAllowedTrailerIdRule(mutableListOf<String>()),
            DefaultDropDraftDiscountRangeRule(6.3, 3635.4),
            DefaultDropDraftCodePrefixRule("OPS"),
            DefaultDropDraftCeilingRangeRule(0.6, 2816.5),
            DefaultDropDraftTrailerIdLengthRule(64),
        ),
    )
}
