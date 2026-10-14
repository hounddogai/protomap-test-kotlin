package com.acme.transport.customs.clearance.clearancecheckpoint

/** A rule a clearance proposal must pass. Check returns its problems, or none. */
interface ClearanceProposalRule {
    fun check(item: ClearanceProposal): MutableList<String>
}

data class ClearanceProposalDiscountRangeRule(var min: Double, var max: Double) : ClearanceProposalRule {
    override fun check(item: ClearanceProposal): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class ClearanceProposalRequiredCodeRule(var message: String) : ClearanceProposalRule {
    override fun check(item: ClearanceProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceProposalTitleLengthRule(var maxLength: Int) : ClearanceProposalRule {
    override fun check(item: ClearanceProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance proposal. */
data class ClearanceProposalValidator(var rules: MutableList<ClearanceProposalRule>) {
    fun validate(item: ClearanceProposal): MutableList<String> {
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

    fun requireValid(item: ClearanceProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceProposalValidator(): ClearanceProposalValidator {
    return ClearanceProposalValidator(
        mutableListOf<ClearanceProposalRule>(
            ClearanceProposalDiscountRangeRule(5.6, 4869.6),
            ClearanceProposalRequiredCodeRule("clearance proposal needs an entry"),
            ClearanceProposalTitleLengthRule(16),
        ),
    )
}
