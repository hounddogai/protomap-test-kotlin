package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.text.normalize

/** A rule a seasonal trailer yard summary must pass. Check returns its problems, or none. */
interface SeasonalTrailerYardSummaryRule {
    fun check(item: SeasonalTrailerYardSummary): MutableList<String>
}

data class SeasonalTrailerYardSummaryDriverIdLengthRule(var maxLength: Int) : SeasonalTrailerYardSummaryRule {
    override fun check(item: SeasonalTrailerYardSummary): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardSummaryCodeLengthRule(var maxLength: Int) : SeasonalTrailerYardSummaryRule {
    override fun check(item: SeasonalTrailerYardSummary): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardSummaryCodePrefixRule(var prefix: String) : SeasonalTrailerYardSummaryRule {
    override fun check(item: SeasonalTrailerYardSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardSummaryRequiredLaneRule(var message: String) : SeasonalTrailerYardSummaryRule {
    override fun check(item: SeasonalTrailerYardSummary): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalTrailerYardSummaryAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : SeasonalTrailerYardSummaryRule {
    override fun check(item: SeasonalTrailerYardSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal trailer yard summary. */
data class SeasonalTrailerYardSummaryValidator(var rules: MutableList<SeasonalTrailerYardSummaryRule>) {
    fun validate(item: SeasonalTrailerYardSummary): MutableList<String> {
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

    fun requireValid(item: SeasonalTrailerYardSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalTrailerYardSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalTrailerYardSummaryValidator(): SeasonalTrailerYardSummaryValidator {
    return SeasonalTrailerYardSummaryValidator(
        mutableListOf<SeasonalTrailerYardSummaryRule>(
            SeasonalTrailerYardSummaryDriverIdLengthRule(32),
            SeasonalTrailerYardSummaryCodeLengthRule(32),
            SeasonalTrailerYardSummaryCodePrefixRule("Q"),
            SeasonalTrailerYardSummaryRequiredLaneRule("seasonal trailer yard summary needs an entry"),
            SeasonalTrailerYardSummaryAllowedDriverIdRule(mutableListOf<String>()),
        ),
    )
}
