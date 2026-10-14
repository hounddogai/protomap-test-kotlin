package com.acme.transport.emissions.carbonreport.reportrule

import com.acme.transport.kit.text.normalize

/** A rule a carbon report proposal must pass. Check returns its problems, or none. */
interface CarbonReportProposalRule {
    fun check(item: CarbonReportProposal): MutableList<String>
}

data class CarbonReportProposalCodePrefixRule(var prefix: String) : CarbonReportProposalRule {
    override fun check(item: CarbonReportProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportProposalEtaMinutesRangeRule(var min: Int, var max: Int) : CarbonReportProposalRule {
    override fun check(item: CarbonReportProposal): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportProposalAllowedSourceRule(var allowed: MutableList<String>) : CarbonReportProposalRule {
    override fun check(item: CarbonReportProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report proposal. */
data class CarbonReportProposalValidator(var rules: MutableList<CarbonReportProposalRule>) {
    fun validate(item: CarbonReportProposal): MutableList<String> {
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

    fun requireValid(item: CarbonReportProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportProposalValidator(): CarbonReportProposalValidator {
    return CarbonReportProposalValidator(
        mutableListOf<CarbonReportProposalRule>(
            CarbonReportProposalCodePrefixRule("ACM"),
            CarbonReportProposalEtaMinutesRangeRule(80, 86400),
            CarbonReportProposalAllowedSourceRule(mutableListOf<String>()),
        ),
    )
}
