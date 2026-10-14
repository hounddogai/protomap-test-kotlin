package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.normalize

/** A rule a pending cutoff score must pass. Check returns its problems, or none. */
interface PendingCutoffScoreRule {
    fun check(item: PendingCutoffScore): MutableList<String>
}

data class PendingCutoffScoreCodePrefixRule(var prefix: String) : PendingCutoffScoreRule {
    override fun check(item: PendingCutoffScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffScoreAllowedLocaleRule(var allowed: MutableList<String>) : PendingCutoffScoreRule {
    override fun check(item: PendingCutoffScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffScoreLabelLengthRule(var maxLength: Int) : PendingCutoffScoreRule {
    override fun check(item: PendingCutoffScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffScoreContactEmailLengthRule(var maxLength: Int) : PendingCutoffScoreRule {
    override fun check(item: PendingCutoffScore): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffScoreRequiredLabelRule(var message: String) : PendingCutoffScoreRule {
    override fun check(item: PendingCutoffScore): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffScoreRateRangeRule(var min: Double, var max: Double) : PendingCutoffScoreRule {
    override fun check(item: PendingCutoffScore): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cutoff score. */
data class PendingCutoffScoreValidator(var rules: MutableList<PendingCutoffScoreRule>) {
    fun validate(item: PendingCutoffScore): MutableList<String> {
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

    fun requireValid(item: PendingCutoffScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCutoffScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCutoffScoreValidator(): PendingCutoffScoreValidator {
    return PendingCutoffScoreValidator(
        mutableListOf<PendingCutoffScoreRule>(
            PendingCutoffScoreCodePrefixRule("Q"),
            PendingCutoffScoreAllowedLocaleRule(mutableListOf<String>("tiered")),
            PendingCutoffScoreLabelLengthRule(32),
            PendingCutoffScoreContactEmailLengthRule(64),
            PendingCutoffScoreRequiredLabelRule("pending cutoff score needs a value"),
            PendingCutoffScoreRateRangeRule(6.2, 2336.1),
        ),
    )
}
