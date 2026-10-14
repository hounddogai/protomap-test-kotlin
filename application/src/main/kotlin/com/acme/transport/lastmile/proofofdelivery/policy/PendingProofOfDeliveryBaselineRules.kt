package com.acme.transport.lastmile.proofofdelivery.policy

import com.acme.transport.kit.text.normalize

/** A rule a pending proof of delivery baseline must pass. Check returns its problems, or none. */
interface PendingProofOfDeliveryBaselineRule {
    fun check(item: PendingProofOfDeliveryBaseline): MutableList<String>
}

data class PendingProofOfDeliveryBaselineBudgetRangeRule(
    var min: Double,
    var max: Double,
) : PendingProofOfDeliveryBaselineRule {
    override fun check(item: PendingProofOfDeliveryBaseline): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class PendingProofOfDeliveryBaselineFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : PendingProofOfDeliveryBaselineRule {
    override fun check(item: PendingProofOfDeliveryBaseline): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryBaselineTitleLengthRule(
    var maxLength: Int,
) : PendingProofOfDeliveryBaselineRule {
    override fun check(item: PendingProofOfDeliveryBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryBaselineCodeLengthRule(
    var maxLength: Int,
) : PendingProofOfDeliveryBaselineRule {
    override fun check(item: PendingProofOfDeliveryBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryBaselineCodePrefixRule(
    var prefix: String,
) : PendingProofOfDeliveryBaselineRule {
    override fun check(item: PendingProofOfDeliveryBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingProofOfDeliveryBaselineAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : PendingProofOfDeliveryBaselineRule {
    override fun check(item: PendingProofOfDeliveryBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending proof of delivery baseline. */
data class PendingProofOfDeliveryBaselineValidator(var rules: MutableList<PendingProofOfDeliveryBaselineRule>) {
    fun validate(item: PendingProofOfDeliveryBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingProofOfDeliveryBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingProofOfDeliveryBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingProofOfDeliveryBaselineValidator(): PendingProofOfDeliveryBaselineValidator {
    return PendingProofOfDeliveryBaselineValidator(
        mutableListOf<PendingProofOfDeliveryBaselineRule>(
            PendingProofOfDeliveryBaselineBudgetRangeRule(8.3, 2681.7),
            PendingProofOfDeliveryBaselineFuelLitersRangeRule(2.2, 604.1),
            PendingProofOfDeliveryBaselineTitleLengthRule(64),
            PendingProofOfDeliveryBaselineCodeLengthRule(3600),
            PendingProofOfDeliveryBaselineCodePrefixRule("Q"),
            PendingProofOfDeliveryBaselineAllowedDescriptionRule(
                mutableListOf<String>("legacy", "regional", "tiered"),
            ),
        ),
    )
}
