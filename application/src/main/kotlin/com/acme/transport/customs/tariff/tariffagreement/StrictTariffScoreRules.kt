package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.text.normalize

/** A rule a strict tariff score must pass. Check returns its problems, or none. */
interface StrictTariffScoreRule {
    fun check(item: StrictTariffScore): MutableList<String>
}

data class StrictTariffScoreTitleLengthRule(var maxLength: Int) : StrictTariffScoreRule {
    override fun check(item: StrictTariffScore): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffScoreAllowedCountryRule(var allowed: MutableList<String>) : StrictTariffScoreRule {
    override fun check(item: StrictTariffScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is too long")
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffScoreRequiredCodeRule(var message: String) : StrictTariffScoreRule {
    override fun check(item: StrictTariffScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffScoreRequiredTitleRule(var message: String) : StrictTariffScoreRule {
    override fun check(item: StrictTariffScore): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffScoreScoreRangeRule(var min: Double, var max: Double) : StrictTariffScoreRule {
    override fun check(item: StrictTariffScore): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictTariffScoreChannelLengthRule(var maxLength: Int) : StrictTariffScoreRule {
    override fun check(item: StrictTariffScore): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict tariff score. */
data class StrictTariffScoreValidator(var rules: MutableList<StrictTariffScoreRule>) {
    fun validate(item: StrictTariffScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictTariffScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictTariffScore>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictTariffScoreValidator(): StrictTariffScoreValidator {
    return StrictTariffScoreValidator(
        mutableListOf<StrictTariffScoreRule>(
            StrictTariffScoreTitleLengthRule(120),
            StrictTariffScoreAllowedCountryRule(mutableListOf<String>("regional")),
            StrictTariffScoreRequiredCodeRule("strict tariff score needs an entry"),
            StrictTariffScoreRequiredTitleRule("strict tariff score needs a value"),
            StrictTariffScoreScoreRangeRule(9.1, 2230.4),
            StrictTariffScoreChannelLengthRule(64),
        ),
    )
}
