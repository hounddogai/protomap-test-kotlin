package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.text.normalize

/** A rule a default proof of delivery score must pass. Check returns its problems, or none. */
interface DefaultProofOfDeliveryScoreRule {
    fun check(item: DefaultProofOfDeliveryScore): MutableList<String>
}

data class DefaultProofOfDeliveryScoreBudgetRangeRule(
    var min: Double,
    var max: Double,
) : DefaultProofOfDeliveryScoreRule {
    override fun check(item: DefaultProofOfDeliveryScore): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryScoreRequiredCodeRule(var message: String) : DefaultProofOfDeliveryScoreRule {
    override fun check(item: DefaultProofOfDeliveryScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryScoreAllowedLocaleRule(
    var allowed: MutableList<String>,
) : DefaultProofOfDeliveryScoreRule {
    override fun check(item: DefaultProofOfDeliveryScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultProofOfDeliveryScoreReviewerLengthRule(var maxLength: Int) : DefaultProofOfDeliveryScoreRule {
    override fun check(item: DefaultProofOfDeliveryScore): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default proof of delivery score. */
data class DefaultProofOfDeliveryScoreValidator(var rules: MutableList<DefaultProofOfDeliveryScoreRule>) {
    fun validate(item: DefaultProofOfDeliveryScore): MutableList<String> {
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

    fun requireValid(item: DefaultProofOfDeliveryScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultProofOfDeliveryScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultProofOfDeliveryScoreValidator(): DefaultProofOfDeliveryScoreValidator {
    return DefaultProofOfDeliveryScoreValidator(
        mutableListOf<DefaultProofOfDeliveryScoreRule>(
            DefaultProofOfDeliveryScoreBudgetRangeRule(3.8, 4770.2),
            DefaultProofOfDeliveryScoreRequiredCodeRule("default proof of delivery score needs this field"),
            DefaultProofOfDeliveryScoreAllowedLocaleRule(mutableListOf<String>("primary", "capped", "rolling")),
            DefaultProofOfDeliveryScoreReviewerLengthRule(32),
        ),
    )
}
