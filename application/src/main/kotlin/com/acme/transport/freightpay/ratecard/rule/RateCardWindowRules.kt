package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize

/** A rule a rate card window must pass. Check returns its problems, or none. */
interface RateCardWindowRule {
    fun check(item: RateCardWindow): MutableList<String>
}

data class RateCardWindowAllowedLabelRule(var allowed: MutableList<String>) : RateCardWindowRule {
    override fun check(item: RateCardWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RateCardWindowCodePrefixRule(var prefix: String) : RateCardWindowRule {
    override fun check(item: RateCardWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardWindowRequiredNameRule(var message: String) : RateCardWindowRule {
    override fun check(item: RateCardWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardWindowRevisionRangeRule(var min: Int, var max: Int) : RateCardWindowRule {
    override fun check(item: RateCardWindow): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class RateCardWindowCodeLengthRule(var maxLength: Int) : RateCardWindowRule {
    override fun check(item: RateCardWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardWindowRequiredLabelRule(var message: String) : RateCardWindowRule {
    override fun check(item: RateCardWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card window. */
data class RateCardWindowValidator(var rules: MutableList<RateCardWindowRule>) {
    fun validate(item: RateCardWindow): MutableList<String> {
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

    fun requireValid(item: RateCardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardWindowValidator(): RateCardWindowValidator {
    return RateCardWindowValidator(
        mutableListOf<RateCardWindowRule>(
            RateCardWindowAllowedLabelRule(mutableListOf<String>("weighted", "pending")),
            RateCardWindowCodePrefixRule("OPS"),
            RateCardWindowRequiredNameRule("rate card window needs a value"),
            RateCardWindowRevisionRangeRule(16, 120),
            RateCardWindowCodeLengthRule(16),
            RateCardWindowRequiredLabelRule("rate card window needs this field"),
        ),
    )
}
