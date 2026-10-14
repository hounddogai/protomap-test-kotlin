package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.text.normalize

/** A rule a regional availability digest must pass. Check returns its problems, or none. */
interface RegionalAvailabilityDigestRule {
    fun check(item: RegionalAvailabilityDigest): MutableList<String>
}

data class RegionalAvailabilityDigestSequenceRangeRule(
    var min: Int,
    var max: Int,
) : RegionalAvailabilityDigestRule {
    override fun check(item: RegionalAvailabilityDigest): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityDigestApprovedGuardRule(var limit: Int) : RegionalAvailabilityDigestRule {
    override fun check(item: RegionalAvailabilityDigest): MutableList<String> {
        if (item.approved && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityDigestAllowedNotesRule(
    var allowed: MutableList<String>,
) : RegionalAvailabilityDigestRule {
    override fun check(item: RegionalAvailabilityDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityDigestNotesLengthRule(var maxLength: Int) : RegionalAvailabilityDigestRule {
    override fun check(item: RegionalAvailabilityDigest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityDigestRequiredCodeRule(var message: String) : RegionalAvailabilityDigestRule {
    override fun check(item: RegionalAvailabilityDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityDigestLabelLengthRule(var maxLength: Int) : RegionalAvailabilityDigestRule {
    override fun check(item: RegionalAvailabilityDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional availability digest. */
data class RegionalAvailabilityDigestValidator(var rules: MutableList<RegionalAvailabilityDigestRule>) {
    fun validate(item: RegionalAvailabilityDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAvailabilityDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAvailabilityDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAvailabilityDigestValidator(): RegionalAvailabilityDigestValidator {
    return RegionalAvailabilityDigestValidator(
        mutableListOf<RegionalAvailabilityDigestRule>(
            RegionalAvailabilityDigestSequenceRangeRule(255, 1000),
            RegionalAvailabilityDigestApprovedGuardRule(80),
            RegionalAvailabilityDigestAllowedNotesRule(mutableListOf<String>()),
            RegionalAvailabilityDigestNotesLengthRule(255),
            RegionalAvailabilityDigestRequiredCodeRule("regional availability digest needs an entry"),
            RegionalAvailabilityDigestLabelLengthRule(80),
        ),
    )
}
