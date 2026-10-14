package com.acme.transport.lastmile.parcel.quota

/** A rule a manual parcel proposal must pass. Check returns its problems, or none. */
interface ManualParcelProposalRule {
    fun check(item: ManualParcelProposal): MutableList<String>
}

data class ManualParcelProposalLabelLengthRule(var maxLength: Int) : ManualParcelProposalRule {
    override fun check(item: ManualParcelProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelProposalRequiredLabelRule(var message: String) : ManualParcelProposalRule {
    override fun check(item: ManualParcelProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelProposalSummaryLengthRule(var maxLength: Int) : ManualParcelProposalRule {
    override fun check(item: ManualParcelProposal): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual parcel proposal. */
data class ManualParcelProposalValidator(var rules: MutableList<ManualParcelProposalRule>) {
    fun validate(item: ManualParcelProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualParcelProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualParcelProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultManualParcelProposalValidator(): ManualParcelProposalValidator {
    return ManualParcelProposalValidator(
        mutableListOf<ManualParcelProposalRule>(
            ManualParcelProposalLabelLengthRule(64),
            ManualParcelProposalRequiredLabelRule("manual parcel proposal needs a value"),
            ManualParcelProposalSummaryLengthRule(120),
        ),
    )
}
