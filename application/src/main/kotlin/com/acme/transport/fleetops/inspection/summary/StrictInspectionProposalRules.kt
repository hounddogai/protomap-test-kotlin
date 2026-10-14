package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.kit.text.normalize

/** A rule a strict inspection proposal must pass. Check returns its problems, or none. */
interface StrictInspectionProposalRule {
    fun check(item: StrictInspectionProposal): MutableList<String>
}

data class StrictInspectionProposalNameLengthRule(var maxLength: Int) : StrictInspectionProposalRule {
    override fun check(item: StrictInspectionProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionProposalCodePrefixRule(var prefix: String) : StrictInspectionProposalRule {
    override fun check(item: StrictInspectionProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictInspectionProposalAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : StrictInspectionProposalRule {
    override fun check(item: StrictInspectionProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict inspection proposal. */
data class StrictInspectionProposalValidator(var rules: MutableList<StrictInspectionProposalRule>) {
    fun validate(item: StrictInspectionProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictInspectionProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictInspectionProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictInspectionProposalValidator(): StrictInspectionProposalValidator {
    return StrictInspectionProposalValidator(
        mutableListOf<StrictInspectionProposalRule>(
            StrictInspectionProposalNameLengthRule(3600),
            StrictInspectionProposalCodePrefixRule("Q"),
            StrictInspectionProposalAllowedOwnerIdRule(mutableListOf<String>()),
        ),
    )
}
