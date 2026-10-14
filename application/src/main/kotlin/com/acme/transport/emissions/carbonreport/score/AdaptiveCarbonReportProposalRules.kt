package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.normalize

/** A rule a adaptive carbon report proposal must pass. Check returns its problems, or none. */
interface AdaptiveCarbonReportProposalRule {
    fun check(item: AdaptiveCarbonReportProposal): MutableList<String>
}

data class AdaptiveCarbonReportProposalRequiredCodeRule(
    var message: String,
) : AdaptiveCarbonReportProposalRule {
    override fun check(item: AdaptiveCarbonReportProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportProposalDockLengthRule(var maxLength: Int) : AdaptiveCarbonReportProposalRule {
    override fun check(item: AdaptiveCarbonReportProposal): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportProposalRequiredDockRule(
    var message: String,
) : AdaptiveCarbonReportProposalRule {
    override fun check(item: AdaptiveCarbonReportProposal): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportProposalStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveCarbonReportProposalRule {
    override fun check(item: AdaptiveCarbonReportProposal): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class AdaptiveCarbonReportProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : AdaptiveCarbonReportProposalRule {
    override fun check(item: AdaptiveCarbonReportProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveCarbonReportProposalCodePrefixRule(var prefix: String) : AdaptiveCarbonReportProposalRule {
    override fun check(item: AdaptiveCarbonReportProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive carbon report proposal. */
data class AdaptiveCarbonReportProposalValidator(var rules: MutableList<AdaptiveCarbonReportProposalRule>) {
    fun validate(item: AdaptiveCarbonReportProposal): MutableList<String> {
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

    fun requireValid(item: AdaptiveCarbonReportProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveCarbonReportProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveCarbonReportProposalValidator(): AdaptiveCarbonReportProposalValidator {
    return AdaptiveCarbonReportProposalValidator(
        mutableListOf<AdaptiveCarbonReportProposalRule>(
            AdaptiveCarbonReportProposalRequiredCodeRule("adaptive carbon report proposal needs a value"),
            AdaptiveCarbonReportProposalDockLengthRule(32),
            AdaptiveCarbonReportProposalRequiredDockRule("adaptive carbon report proposal needs an entry"),
            AdaptiveCarbonReportProposalStartsAtRangeRule(32, 80),
            AdaptiveCarbonReportProposalAllowedCodeRule(mutableListOf<String>("manual", "capped")),
            AdaptiveCarbonReportProposalCodePrefixRule("ACM"),
        ),
    )
}
