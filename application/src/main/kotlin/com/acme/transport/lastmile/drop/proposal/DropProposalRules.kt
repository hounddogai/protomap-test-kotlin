package com.acme.transport.lastmile.drop.proposal

/** A rule a drop proposal must pass. Check returns its problems, or none. */
interface DropProposalRule {
    fun check(item: DropProposal): MutableList<String>
}

data class DropProposalCodeLengthRule(var maxLength: Int) : DropProposalRule {
    override fun check(item: DropProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropProposalLabelLengthRule(var maxLength: Int) : DropProposalRule {
    override fun check(item: DropProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropProposalDiscountRangeRule(var min: Double, var max: Double) : DropProposalRule {
    override fun check(item: DropProposal): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DropProposalRequiredSourceRule(var message: String) : DropProposalRule {
    override fun check(item: DropProposal): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropProposalSourceLengthRule(var maxLength: Int) : DropProposalRule {
    override fun check(item: DropProposal): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropProposalScoreRangeRule(var min: Double, var max: Double) : DropProposalRule {
    override fun check(item: DropProposal): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a drop proposal. */
data class DropProposalValidator(var rules: MutableList<DropProposalRule>) {
    fun validate(item: DropProposal): MutableList<String> {
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

    fun requireValid(item: DropProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDropProposalValidator(): DropProposalValidator {
    return DropProposalValidator(
        mutableListOf<DropProposalRule>(
            DropProposalCodeLengthRule(64),
            DropProposalLabelLengthRule(120),
            DropProposalDiscountRangeRule(4.2, 1502.8),
            DropProposalRequiredSourceRule("drop proposal needs this field"),
            DropProposalSourceLengthRule(86400),
            DropProposalScoreRangeRule(0.1, 268.0),
        ),
    )
}
