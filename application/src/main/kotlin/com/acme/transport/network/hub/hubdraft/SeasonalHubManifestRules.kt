package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.text.normalize

/** A rule a seasonal hub manifest must pass. Check returns its problems, or none. */
interface SeasonalHubManifestRule {
    fun check(item: SeasonalHubManifest): MutableList<String>
}

data class SeasonalHubManifestRequiredCodeRule(var message: String) : SeasonalHubManifestRule {
    override fun check(item: SeasonalHubManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubManifestRequiredTitleRule(var message: String) : SeasonalHubManifestRule {
    override fun check(item: SeasonalHubManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubManifestTitleLengthRule(var maxLength: Int) : SeasonalHubManifestRule {
    override fun check(item: SeasonalHubManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubManifestAllowedCodeRule(var allowed: MutableList<String>) : SeasonalHubManifestRule {
    override fun check(item: SeasonalHubManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalHubManifestRequiredNameRule(var message: String) : SeasonalHubManifestRule {
    override fun check(item: SeasonalHubManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal hub manifest. */
data class SeasonalHubManifestValidator(var rules: MutableList<SeasonalHubManifestRule>) {
    fun validate(item: SeasonalHubManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalHubManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalHubManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalHubManifestValidator(): SeasonalHubManifestValidator {
    return SeasonalHubManifestValidator(
        mutableListOf<SeasonalHubManifestRule>(
            SeasonalHubManifestRequiredCodeRule("seasonal hub manifest needs an entry"),
            SeasonalHubManifestRequiredTitleRule("seasonal hub manifest needs an entry"),
            SeasonalHubManifestTitleLengthRule(64),
            SeasonalHubManifestAllowedCodeRule(mutableListOf<String>()),
            SeasonalHubManifestRequiredNameRule("seasonal hub manifest needs a value"),
        ),
    )
}
