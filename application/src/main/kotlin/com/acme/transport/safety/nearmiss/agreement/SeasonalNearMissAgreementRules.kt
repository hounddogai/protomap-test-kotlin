package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.text.normalize

/** A rule a seasonal near miss agreement must pass. Check returns its problems, or none. */
interface SeasonalNearMissAgreementRule {
    fun check(item: SeasonalNearMissAgreement): MutableList<String>
}

data class SeasonalNearMissAgreementSourceLengthRule(var maxLength: Int) : SeasonalNearMissAgreementRule {
    override fun check(item: SeasonalNearMissAgreement): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissAgreementArchivedGuardRule(var limit: Int) : SeasonalNearMissAgreementRule {
    override fun check(item: SeasonalNearMissAgreement): MutableList<String> {
        if (item.archived && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissAgreementAllowedLabelRule(
    var allowed: MutableList<String>,
) : SeasonalNearMissAgreementRule {
    override fun check(item: SeasonalNearMissAgreement): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal near miss agreement. */
data class SeasonalNearMissAgreementValidator(var rules: MutableList<SeasonalNearMissAgreementRule>) {
    fun validate(item: SeasonalNearMissAgreement): MutableList<String> {
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

    fun requireValid(item: SeasonalNearMissAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalNearMissAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalNearMissAgreementValidator(): SeasonalNearMissAgreementValidator {
    return SeasonalNearMissAgreementValidator(
        mutableListOf<SeasonalNearMissAgreementRule>(
            SeasonalNearMissAgreementSourceLengthRule(80),
            SeasonalNearMissAgreementArchivedGuardRule(3600),
            SeasonalNearMissAgreementAllowedLabelRule(mutableListOf<String>("batched", "primary", "manual")),
        ),
    )
}
