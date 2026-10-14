package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.kit.text.normalize

/** A rule a strict broker draft must pass. Check returns its problems, or none. */
interface StrictBrokerDraftRule {
    fun check(item: StrictBrokerDraft): MutableList<String>
}

data class StrictBrokerDraftCodePrefixRule(var prefix: String) : StrictBrokerDraftRule {
    override fun check(item: StrictBrokerDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerDraftAllowedLabelRule(var allowed: MutableList<String>) : StrictBrokerDraftRule {
    override fun check(item: StrictBrokerDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class StrictBrokerDraftAmountRangeRule(var min: Double, var max: Double) : StrictBrokerDraftRule {
    override fun check(item: StrictBrokerDraft): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class StrictBrokerDraftTimezoneLengthRule(var maxLength: Int) : StrictBrokerDraftRule {
    override fun check(item: StrictBrokerDraft): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict broker draft. */
data class StrictBrokerDraftValidator(var rules: MutableList<StrictBrokerDraftRule>) {
    fun validate(item: StrictBrokerDraft): MutableList<String> {
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

    fun requireValid(item: StrictBrokerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictBrokerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictBrokerDraftValidator(): StrictBrokerDraftValidator {
    return StrictBrokerDraftValidator(
        mutableListOf<StrictBrokerDraftRule>(
            StrictBrokerDraftCodePrefixRule("Q"),
            StrictBrokerDraftAllowedLabelRule(mutableListOf<String>("batched", "scheduled", "weighted")),
            StrictBrokerDraftAmountRangeRule(5.4, 684.6),
            StrictBrokerDraftTimezoneLengthRule(32),
        ),
    )
}
