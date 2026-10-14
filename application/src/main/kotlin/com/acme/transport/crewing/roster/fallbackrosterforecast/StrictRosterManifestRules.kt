package com.acme.transport.crewing.roster.fallbackrosterforecast

import com.acme.transport.kit.text.normalize

/** A rule a strict roster manifest must pass. Check returns its problems, or none. */
interface StrictRosterManifestRule {
    fun check(item: StrictRosterManifest): MutableList<String>
}

data class StrictRosterManifestRequiredLabelRule(var message: String) : StrictRosterManifestRule {
    override fun check(item: StrictRosterManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterManifestLabelLengthRule(var maxLength: Int) : StrictRosterManifestRule {
    override fun check(item: StrictRosterManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterManifestAllowedLabelRule(var allowed: MutableList<String>) : StrictRosterManifestRule {
    override fun check(item: StrictRosterManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict roster manifest. */
data class StrictRosterManifestValidator(var rules: MutableList<StrictRosterManifestRule>) {
    fun validate(item: StrictRosterManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRosterManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRosterManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRosterManifestValidator(): StrictRosterManifestValidator {
    return StrictRosterManifestValidator(
        mutableListOf<StrictRosterManifestRule>(
            StrictRosterManifestRequiredLabelRule("strict roster manifest needs an entry"),
            StrictRosterManifestLabelLengthRule(86400),
            StrictRosterManifestAllowedLabelRule(mutableListOf<String>("rolling")),
        ),
    )
}
