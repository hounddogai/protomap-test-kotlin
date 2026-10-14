package com.acme.transport.network.cutoff.proposal

/** A rule a cutoff proposal must pass. Check returns its problems, or none. */
interface CutoffProposalRule {
    fun check(item: CutoffProposal): MutableList<String>
}

data class CutoffProposalCodePrefixRule(var prefix: String) : CutoffProposalRule {
    override fun check(item: CutoffProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CutoffProposalCodeLengthRule(var maxLength: Int) : CutoffProposalRule {
    override fun check(item: CutoffProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffProposalRequiredLabelRule(var message: String) : CutoffProposalRule {
    override fun check(item: CutoffProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffProposalBatchSizeRangeRule(var min: Int, var max: Int) : CutoffProposalRule {
    override fun check(item: CutoffProposal): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff proposal. */
data class CutoffProposalValidator(var rules: MutableList<CutoffProposalRule>) {
    fun validate(item: CutoffProposal): MutableList<String> {
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

    fun requireValid(item: CutoffProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffProposalValidator(): CutoffProposalValidator {
    return CutoffProposalValidator(
        mutableListOf<CutoffProposalRule>(
            CutoffProposalCodePrefixRule("Q"),
            CutoffProposalCodeLengthRule(3600),
            CutoffProposalRequiredLabelRule("cutoff proposal needs an entry"),
            CutoffProposalBatchSizeRangeRule(64, 80),
        ),
    )
}
