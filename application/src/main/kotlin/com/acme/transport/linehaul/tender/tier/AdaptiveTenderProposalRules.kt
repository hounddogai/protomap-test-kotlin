package com.acme.transport.linehaul.tender.tier

/** A rule a adaptive tender proposal must pass. Check returns its problems, or none. */
interface AdaptiveTenderProposalRule {
    fun check(item: AdaptiveTenderProposal): MutableList<String>
}

data class AdaptiveTenderProposalChecksumLengthRule(var maxLength: Int) : AdaptiveTenderProposalRule {
    override fun check(item: AdaptiveTenderProposal): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderProposalRequiredCodeRule(var message: String) : AdaptiveTenderProposalRule {
    override fun check(item: AdaptiveTenderProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderProposalMinimumRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveTenderProposalRule {
    override fun check(item: AdaptiveTenderProposal): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderProposalCodePrefixRule(var prefix: String) : AdaptiveTenderProposalRule {
    override fun check(item: AdaptiveTenderProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveTenderProposalRequiredChecksumRule(var message: String) : AdaptiveTenderProposalRule {
    override fun check(item: AdaptiveTenderProposal): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive tender proposal. */
data class AdaptiveTenderProposalValidator(var rules: MutableList<AdaptiveTenderProposalRule>) {
    fun validate(item: AdaptiveTenderProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveTenderProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveTenderProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveTenderProposalValidator(): AdaptiveTenderProposalValidator {
    return AdaptiveTenderProposalValidator(
        mutableListOf<AdaptiveTenderProposalRule>(
            AdaptiveTenderProposalChecksumLengthRule(32),
            AdaptiveTenderProposalRequiredCodeRule("adaptive tender proposal needs a value"),
            AdaptiveTenderProposalMinimumRangeRule(6.7, 635.5),
            AdaptiveTenderProposalCodePrefixRule("ACM"),
            AdaptiveTenderProposalRequiredChecksumRule("adaptive tender proposal needs a value"),
        ),
    )
}
