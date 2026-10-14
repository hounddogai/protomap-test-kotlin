package com.acme.transport.fleetops.inspection.group

/** A rule a inspection proposal must pass. Check returns its problems, or none. */
interface InspectionProposalRule {
    fun check(item: InspectionProposal): MutableList<String>
}

data class InspectionProposalTitleLengthRule(var maxLength: Int) : InspectionProposalRule {
    override fun check(item: InspectionProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionProposalRequiredRouteCodeRule(var message: String) : InspectionProposalRule {
    override fun check(item: InspectionProposal): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionProposalCodeLengthRule(var maxLength: Int) : InspectionProposalRule {
    override fun check(item: InspectionProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionProposalRequiredSummaryRule(var message: String) : InspectionProposalRule {
    override fun check(item: InspectionProposal): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection proposal. */
data class InspectionProposalValidator(var rules: MutableList<InspectionProposalRule>) {
    fun validate(item: InspectionProposal): MutableList<String> {
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

    fun requireValid(item: InspectionProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionProposalValidator(): InspectionProposalValidator {
    return InspectionProposalValidator(
        mutableListOf<InspectionProposalRule>(
            InspectionProposalTitleLengthRule(1000),
            InspectionProposalRequiredRouteCodeRule("inspection proposal needs a value"),
            InspectionProposalCodeLengthRule(120),
            InspectionProposalRequiredSummaryRule("inspection proposal needs an entry"),
        ),
    )
}
