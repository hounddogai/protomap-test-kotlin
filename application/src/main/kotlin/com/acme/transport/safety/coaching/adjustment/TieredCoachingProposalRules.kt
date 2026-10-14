package com.acme.transport.safety.coaching.adjustment

/** A rule a tiered coaching proposal must pass. Check returns its problems, or none. */
interface TieredCoachingProposalRule {
    fun check(item: TieredCoachingProposal): MutableList<String>
}

data class TieredCoachingProposalRequiredChecksumRule(var message: String) : TieredCoachingProposalRule {
    override fun check(item: TieredCoachingProposal): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCoachingProposalCodePrefixRule(var prefix: String) : TieredCoachingProposalRule {
    override fun check(item: TieredCoachingProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCoachingProposalNameLengthRule(var maxLength: Int) : TieredCoachingProposalRule {
    override fun check(item: TieredCoachingProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCoachingProposalRequiredNameRule(var message: String) : TieredCoachingProposalRule {
    override fun check(item: TieredCoachingProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered coaching proposal. */
data class TieredCoachingProposalValidator(var rules: MutableList<TieredCoachingProposalRule>) {
    fun validate(item: TieredCoachingProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCoachingProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCoachingProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCoachingProposalValidator(): TieredCoachingProposalValidator {
    return TieredCoachingProposalValidator(
        mutableListOf<TieredCoachingProposalRule>(
            TieredCoachingProposalRequiredChecksumRule("tiered coaching proposal needs a value"),
            TieredCoachingProposalCodePrefixRule("ACM"),
            TieredCoachingProposalNameLengthRule(255),
            TieredCoachingProposalRequiredNameRule("tiered coaching proposal needs this field"),
        ),
    )
}
