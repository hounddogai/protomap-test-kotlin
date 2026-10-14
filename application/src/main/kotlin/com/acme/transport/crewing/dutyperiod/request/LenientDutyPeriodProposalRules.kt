package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.text.normalize

/** A rule a lenient duty period proposal must pass. Check returns its problems, or none. */
interface LenientDutyPeriodProposalRule {
    fun check(item: LenientDutyPeriodProposal): MutableList<String>
}

data class LenientDutyPeriodProposalNameLengthRule(var maxLength: Int) : LenientDutyPeriodProposalRule {
    override fun check(item: LenientDutyPeriodProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodProposalCodeLengthRule(var maxLength: Int) : LenientDutyPeriodProposalRule {
    override fun check(item: LenientDutyPeriodProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodProposalAllowedNameRule(
    var allowed: MutableList<String>,
) : LenientDutyPeriodProposalRule {
    override fun check(item: LenientDutyPeriodProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodProposalTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LenientDutyPeriodProposalRule {
    override fun check(item: LenientDutyPeriodProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LenientDutyPeriodProposalRequiredNameRule(var message: String) : LenientDutyPeriodProposalRule {
    override fun check(item: LenientDutyPeriodProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDutyPeriodProposalEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientDutyPeriodProposalRule {
    override fun check(item: LenientDutyPeriodProposal): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient duty period proposal. */
data class LenientDutyPeriodProposalValidator(var rules: MutableList<LenientDutyPeriodProposalRule>) {
    fun validate(item: LenientDutyPeriodProposal): MutableList<String> {
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

    fun requireValid(item: LenientDutyPeriodProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDutyPeriodProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDutyPeriodProposalValidator(): LenientDutyPeriodProposalValidator {
    return LenientDutyPeriodProposalValidator(
        mutableListOf<LenientDutyPeriodProposalRule>(
            LenientDutyPeriodProposalNameLengthRule(120),
            LenientDutyPeriodProposalCodeLengthRule(80),
            LenientDutyPeriodProposalAllowedNameRule(mutableListOf<String>()),
            LenientDutyPeriodProposalTagsLimitRule(
                3600,
                mutableListOf<String>("regional", "strict", "seasonal"),
            ),
            LenientDutyPeriodProposalRequiredNameRule("lenient duty period proposal needs a value"),
            LenientDutyPeriodProposalEndsAtRangeRule(16, 32),
        ),
    )
}
