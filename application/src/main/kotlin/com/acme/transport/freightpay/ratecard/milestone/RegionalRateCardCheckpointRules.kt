package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.kit.text.normalize

/** A rule a regional rate card checkpoint must pass. Check returns its problems, or none. */
interface RegionalRateCardCheckpointRule {
    fun check(item: RegionalRateCardCheckpoint): MutableList<String>
}

data class RegionalRateCardCheckpointSequenceRangeRule(
    var min: Int,
    var max: Int,
) : RegionalRateCardCheckpointRule {
    override fun check(item: RegionalRateCardCheckpoint): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardCheckpointRequiredCodeRule(var message: String) : RegionalRateCardCheckpointRule {
    override fun check(item: RegionalRateCardCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardCheckpointAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalRateCardCheckpointRule {
    override fun check(item: RegionalRateCardCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRateCardCheckpointRequiredTitleRule(var message: String) : RegionalRateCardCheckpointRule {
    override fun check(item: RegionalRateCardCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional rate card checkpoint. */
data class RegionalRateCardCheckpointValidator(var rules: MutableList<RegionalRateCardCheckpointRule>) {
    fun validate(item: RegionalRateCardCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRateCardCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRateCardCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRateCardCheckpointValidator(): RegionalRateCardCheckpointValidator {
    return RegionalRateCardCheckpointValidator(
        mutableListOf<RegionalRateCardCheckpointRule>(
            RegionalRateCardCheckpointSequenceRangeRule(255, 86400),
            RegionalRateCardCheckpointRequiredCodeRule("regional rate card checkpoint needs an entry"),
            RegionalRateCardCheckpointAllowedTitleRule(mutableListOf<String>("fallback", "batched")),
            RegionalRateCardCheckpointRequiredTitleRule("regional rate card checkpoint needs a value"),
        ),
    )
}
