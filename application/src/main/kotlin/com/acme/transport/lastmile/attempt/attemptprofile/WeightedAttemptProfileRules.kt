package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.text.normalize

/** A rule a weighted attempt profile must pass. Check returns its problems, or none. */
interface WeightedAttemptProfileRule {
    fun check(item: WeightedAttemptProfile): MutableList<String>
}

data class WeightedAttemptProfileRequiredExternalRefRule(var message: String) : WeightedAttemptProfileRule {
    override fun check(item: WeightedAttemptProfile): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAttemptProfileCodeLengthRule(var maxLength: Int) : WeightedAttemptProfileRule {
    override fun check(item: WeightedAttemptProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAttemptProfileSequenceRangeRule(var min: Int, var max: Int) : WeightedAttemptProfileRule {
    override fun check(item: WeightedAttemptProfile): MutableList<String> {
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

data class WeightedAttemptProfileAllowedExternalRefRule(
    var allowed: MutableList<String>,
) : WeightedAttemptProfileRule {
    override fun check(item: WeightedAttemptProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.externalRef))) {
            return mutableListOf<String>("external ref ${item.externalRef} is stale")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAttemptProfileCodePrefixRule(var prefix: String) : WeightedAttemptProfileRule {
    override fun check(item: WeightedAttemptProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAttemptProfileRequiredTitleRule(var message: String) : WeightedAttemptProfileRule {
    override fun check(item: WeightedAttemptProfile): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted attempt profile. */
data class WeightedAttemptProfileValidator(var rules: MutableList<WeightedAttemptProfileRule>) {
    fun validate(item: WeightedAttemptProfile): MutableList<String> {
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

    fun requireValid(item: WeightedAttemptProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAttemptProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAttemptProfileValidator(): WeightedAttemptProfileValidator {
    return WeightedAttemptProfileValidator(
        mutableListOf<WeightedAttemptProfileRule>(
            WeightedAttemptProfileRequiredExternalRefRule("weighted attempt profile needs this field"),
            WeightedAttemptProfileCodeLengthRule(255),
            WeightedAttemptProfileSequenceRangeRule(32, 120),
            WeightedAttemptProfileAllowedExternalRefRule(mutableListOf<String>()),
            WeightedAttemptProfileCodePrefixRule("Q"),
            WeightedAttemptProfileRequiredTitleRule("weighted attempt profile needs an entry"),
        ),
    )
}
