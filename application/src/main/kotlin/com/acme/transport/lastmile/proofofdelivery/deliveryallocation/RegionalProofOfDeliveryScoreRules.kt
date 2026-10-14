package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.normalize

/** A rule a regional proof of delivery score must pass. Check returns its problems, or none. */
interface RegionalProofOfDeliveryScoreRule {
    fun check(item: RegionalProofOfDeliveryScore): MutableList<String>
}

data class RegionalProofOfDeliveryScoreCodeLengthRule(var maxLength: Int) : RegionalProofOfDeliveryScoreRule {
    override fun check(item: RegionalProofOfDeliveryScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryScoreAllowedLabelRule(
    var allowed: MutableList<String>,
) : RegionalProofOfDeliveryScoreRule {
    override fun check(item: RegionalProofOfDeliveryScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryScoreLabelLengthRule(var maxLength: Int) : RegionalProofOfDeliveryScoreRule {
    override fun check(item: RegionalProofOfDeliveryScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryScoreDockLengthRule(var maxLength: Int) : RegionalProofOfDeliveryScoreRule {
    override fun check(item: RegionalProofOfDeliveryScore): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalProofOfDeliveryScoreAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalProofOfDeliveryScoreRule {
    override fun check(item: RegionalProofOfDeliveryScore): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class RegionalProofOfDeliveryScoreSequenceRangeRule(
    var min: Int,
    var max: Int,
) : RegionalProofOfDeliveryScoreRule {
    override fun check(item: RegionalProofOfDeliveryScore): MutableList<String> {
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

/** Runs every rule of a regional proof of delivery score. */
data class RegionalProofOfDeliveryScoreValidator(var rules: MutableList<RegionalProofOfDeliveryScoreRule>) {
    fun validate(item: RegionalProofOfDeliveryScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalProofOfDeliveryScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalProofOfDeliveryScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalProofOfDeliveryScoreValidator(): RegionalProofOfDeliveryScoreValidator {
    return RegionalProofOfDeliveryScoreValidator(
        mutableListOf<RegionalProofOfDeliveryScoreRule>(
            RegionalProofOfDeliveryScoreCodeLengthRule(32),
            RegionalProofOfDeliveryScoreAllowedLabelRule(mutableListOf<String>("capped", "global", "primary")),
            RegionalProofOfDeliveryScoreLabelLengthRule(120),
            RegionalProofOfDeliveryScoreDockLengthRule(16),
            RegionalProofOfDeliveryScoreAttemptsRangeRule(64, 120),
            RegionalProofOfDeliveryScoreSequenceRangeRule(32, 86400),
        ),
    )
}
