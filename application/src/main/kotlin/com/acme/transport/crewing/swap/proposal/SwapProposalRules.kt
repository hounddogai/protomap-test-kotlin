package com.acme.transport.crewing.swap.proposal

/** A rule a swap proposal must pass. Check returns its problems, or none. */
interface SwapProposalRule {
    fun check(item: SwapProposal): MutableList<String>
}

data class SwapProposalMarginRangeRule(var min: Double, var max: Double) : SwapProposalRule {
    override fun check(item: SwapProposal): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SwapProposalCategoryLengthRule(var maxLength: Int) : SwapProposalRule {
    override fun check(item: SwapProposal): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SwapProposalCodePrefixRule(var prefix: String) : SwapProposalRule {
    override fun check(item: SwapProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SwapProposalRequiredCodeRule(var message: String) : SwapProposalRule {
    override fun check(item: SwapProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a swap proposal. */
data class SwapProposalValidator(var rules: MutableList<SwapProposalRule>) {
    fun validate(item: SwapProposal): MutableList<String> {
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

    fun requireValid(item: SwapProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SwapProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultSwapProposalValidator(): SwapProposalValidator {
    return SwapProposalValidator(
        mutableListOf<SwapProposalRule>(
            SwapProposalMarginRangeRule(2.2, 4967.0),
            SwapProposalCategoryLengthRule(1000),
            SwapProposalCodePrefixRule("REF"),
            SwapProposalRequiredCodeRule("swap proposal needs this field"),
        ),
    )
}
