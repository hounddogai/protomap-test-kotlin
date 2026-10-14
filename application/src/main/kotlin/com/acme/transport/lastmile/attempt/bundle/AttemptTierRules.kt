package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.text.normalize

/** A rule a attempt tier must pass. Check returns its problems, or none. */
interface AttemptTierRule {
    fun check(item: AttemptTier): MutableList<String>
}

data class AttemptTierAllowedLocaleRule(var allowed: MutableList<String>) : AttemptTierRule {
    override fun check(item: AttemptTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class AttemptTierEtaMinutesRangeRule(var min: Int, var max: Int) : AttemptTierRule {
    override fun check(item: AttemptTier): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptTierCodeLengthRule(var maxLength: Int) : AttemptTierRule {
    override fun check(item: AttemptTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptTierApprovedGuardRule(var limit: Int) : AttemptTierRule {
    override fun check(item: AttemptTier): MutableList<String> {
        if (item.approved && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class AttemptTierRequiredReviewerRule(var message: String) : AttemptTierRule {
    override fun check(item: AttemptTier): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptTierRequiredLabelRule(var message: String) : AttemptTierRule {
    override fun check(item: AttemptTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt tier. */
data class AttemptTierValidator(var rules: MutableList<AttemptTierRule>) {
    fun validate(item: AttemptTier): MutableList<String> {
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

    fun requireValid(item: AttemptTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptTier>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptTierValidator(): AttemptTierValidator {
    return AttemptTierValidator(
        mutableListOf<AttemptTierRule>(
            AttemptTierAllowedLocaleRule(mutableListOf<String>("adaptive", "global", "strict")),
            AttemptTierEtaMinutesRangeRule(64, 120),
            AttemptTierCodeLengthRule(255),
            AttemptTierApprovedGuardRule(255),
            AttemptTierRequiredReviewerRule("attempt tier needs a value"),
            AttemptTierRequiredLabelRule("attempt tier needs a value"),
        ),
    )
}
