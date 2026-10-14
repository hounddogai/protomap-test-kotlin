package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.normalize

/** A rule a seasonal hub entry must pass. Check returns its problems, or none. */
interface SeasonalHubEntryRule {
    fun check(item: SeasonalHubEntry): MutableList<String>
}

data class SeasonalHubEntryCountryLengthRule(var maxLength: Int) : SeasonalHubEntryRule {
    override fun check(item: SeasonalHubEntry): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubEntryRequiredCodeRule(var message: String) : SeasonalHubEntryRule {
    override fun check(item: SeasonalHubEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubEntryAllowedCodeRule(var allowed: MutableList<String>) : SeasonalHubEntryRule {
    override fun check(item: SeasonalHubEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal hub entry. */
data class SeasonalHubEntryValidator(var rules: MutableList<SeasonalHubEntryRule>) {
    fun validate(item: SeasonalHubEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalHubEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalHubEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalHubEntryValidator(): SeasonalHubEntryValidator {
    return SeasonalHubEntryValidator(
        mutableListOf<SeasonalHubEntryRule>(
            SeasonalHubEntryCountryLengthRule(86400),
            SeasonalHubEntryRequiredCodeRule("seasonal hub entry needs a value"),
            SeasonalHubEntryAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
