package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.text.normalize

/** A rule a pending scorecard threshold must pass. Check returns its problems, or none. */
interface PendingScorecardThresholdRule {
    fun check(item: PendingScorecardThreshold): MutableList<String>
}

data class PendingScorecardThresholdPalletsRangeRule(
    var min: Int,
    var max: Int,
) : PendingScorecardThresholdRule {
    override fun check(item: PendingScorecardThreshold): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class PendingScorecardThresholdRequiredTitleRule(var message: String) : PendingScorecardThresholdRule {
    override fun check(item: PendingScorecardThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardThresholdChannelLengthRule(var maxLength: Int) : PendingScorecardThresholdRule {
    override fun check(item: PendingScorecardThreshold): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardThresholdLabelLengthRule(var maxLength: Int) : PendingScorecardThresholdRule {
    override fun check(item: PendingScorecardThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardThresholdTitleLengthRule(var maxLength: Int) : PendingScorecardThresholdRule {
    override fun check(item: PendingScorecardThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingScorecardThresholdAllowedLocaleRule(
    var allowed: MutableList<String>,
) : PendingScorecardThresholdRule {
    override fun check(item: PendingScorecardThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending scorecard threshold. */
data class PendingScorecardThresholdValidator(var rules: MutableList<PendingScorecardThresholdRule>) {
    fun validate(item: PendingScorecardThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingScorecardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingScorecardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingScorecardThresholdValidator(): PendingScorecardThresholdValidator {
    return PendingScorecardThresholdValidator(
        mutableListOf<PendingScorecardThresholdRule>(
            PendingScorecardThresholdPalletsRangeRule(255, 3600),
            PendingScorecardThresholdRequiredTitleRule("pending scorecard threshold needs this field"),
            PendingScorecardThresholdChannelLengthRule(64),
            PendingScorecardThresholdLabelLengthRule(86400),
            PendingScorecardThresholdTitleLengthRule(86400),
            PendingScorecardThresholdAllowedLocaleRule(mutableListOf<String>()),
        ),
    )
}
