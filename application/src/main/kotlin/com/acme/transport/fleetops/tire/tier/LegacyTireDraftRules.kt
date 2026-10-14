package com.acme.transport.fleetops.tire.tier

/** A rule a legacy tire draft must pass. Check returns its problems, or none. */
interface LegacyTireDraftRule {
    fun check(item: LegacyTireDraft): MutableList<String>
}

data class LegacyTireDraftSourceLengthRule(var maxLength: Int) : LegacyTireDraftRule {
    override fun check(item: LegacyTireDraft): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireDraftSummaryLengthRule(var maxLength: Int) : LegacyTireDraftRule {
    override fun check(item: LegacyTireDraft): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTireDraftRequiredSourceRule(var message: String) : LegacyTireDraftRule {
    override fun check(item: LegacyTireDraft): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tire draft. */
data class LegacyTireDraftValidator(var rules: MutableList<LegacyTireDraftRule>) {
    fun validate(item: LegacyTireDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTireDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTireDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTireDraftValidator(): LegacyTireDraftValidator {
    return LegacyTireDraftValidator(
        mutableListOf<LegacyTireDraftRule>(
            LegacyTireDraftSourceLengthRule(64),
            LegacyTireDraftSummaryLengthRule(86400),
            LegacyTireDraftRequiredSourceRule("legacy tire draft needs an entry"),
        ),
    )
}
