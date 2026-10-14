package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.normalize

/** A rule a defect proposal must pass. Check returns its problems, or none. */
interface DefectProposalRule {
    fun check(item: DefectProposal): MutableList<String>
}

data class DefectProposalRequiredCategoryRule(var message: String) : DefectProposalRule {
    override fun check(item: DefectProposal): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectProposalAllowedCategoryRule(var allowed: MutableList<String>) : DefectProposalRule {
    override fun check(item: DefectProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DefectProposalRequiredCodeRule(var message: String) : DefectProposalRule {
    override fun check(item: DefectProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectProposalCodeLengthRule(var maxLength: Int) : DefectProposalRule {
    override fun check(item: DefectProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect proposal. */
data class DefectProposalValidator(var rules: MutableList<DefectProposalRule>) {
    fun validate(item: DefectProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectProposalValidator(): DefectProposalValidator {
    return DefectProposalValidator(
        mutableListOf<DefectProposalRule>(
            DefectProposalRequiredCategoryRule("defect proposal needs an entry"),
            DefectProposalAllowedCategoryRule(mutableListOf<String>("weighted")),
            DefectProposalRequiredCodeRule("defect proposal needs a value"),
            DefectProposalCodeLengthRule(3600),
        ),
    )
}
