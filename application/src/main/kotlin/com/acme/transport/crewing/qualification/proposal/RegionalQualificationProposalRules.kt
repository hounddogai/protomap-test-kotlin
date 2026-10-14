package com.acme.transport.crewing.qualification.proposal

import com.acme.transport.kit.text.normalize

/** A rule a regional qualification proposal must pass. Check returns its problems, or none. */
interface RegionalQualificationProposalRule {
    fun check(item: RegionalQualificationProposal): MutableList<String>
}

data class RegionalQualificationProposalAllowedTitleRule(
    var allowed: MutableList<String>,
) : RegionalQualificationProposalRule {
    override fun check(item: RegionalQualificationProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationProposalRetriesRangeRule(
    var min: Int,
    var max: Int,
) : RegionalQualificationProposalRule {
    override fun check(item: RegionalQualificationProposal): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationProposalFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : RegionalQualificationProposalRule {
    override fun check(item: RegionalQualificationProposal): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class RegionalQualificationProposalCodePrefixRule(var prefix: String) : RegionalQualificationProposalRule {
    override fun check(item: RegionalQualificationProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationProposalRequiredTitleRule(
    var message: String,
) : RegionalQualificationProposalRule {
    override fun check(item: RegionalQualificationProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional qualification proposal. */
data class RegionalQualificationProposalValidator(var rules: MutableList<RegionalQualificationProposalRule>) {
    fun validate(item: RegionalQualificationProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalQualificationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalQualificationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalQualificationProposalValidator(): RegionalQualificationProposalValidator {
    return RegionalQualificationProposalValidator(
        mutableListOf<RegionalQualificationProposalRule>(
            RegionalQualificationProposalAllowedTitleRule(mutableListOf<String>()),
            RegionalQualificationProposalRetriesRangeRule(64, 64),
            RegionalQualificationProposalFloorValueRangeRule(5.1, 4230.6),
            RegionalQualificationProposalCodePrefixRule("Q"),
            RegionalQualificationProposalRequiredTitleRule("regional qualification proposal needs a value"),
        ),
    )
}
