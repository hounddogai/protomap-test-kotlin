package com.acme.transport.crewing.roster.fallbackrosterforecast

/** A rule a primary roster proposal must pass. Check returns its problems, or none. */
interface PrimaryRosterProposalRule {
    fun check(item: PrimaryRosterProposal): MutableList<String>
}

data class PrimaryRosterProposalRequiredNameRule(var message: String) : PrimaryRosterProposalRule {
    override fun check(item: PrimaryRosterProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterProposalRequiredCategoryRule(var message: String) : PrimaryRosterProposalRule {
    override fun check(item: PrimaryRosterProposal): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterProposalCodePrefixRule(var prefix: String) : PrimaryRosterProposalRule {
    override fun check(item: PrimaryRosterProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterProposalLaneLengthRule(var maxLength: Int) : PrimaryRosterProposalRule {
    override fun check(item: PrimaryRosterProposal): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterProposalCodeLengthRule(var maxLength: Int) : PrimaryRosterProposalRule {
    override fun check(item: PrimaryRosterProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary roster proposal. */
data class PrimaryRosterProposalValidator(var rules: MutableList<PrimaryRosterProposalRule>) {
    fun validate(item: PrimaryRosterProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryRosterProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRosterProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRosterProposalValidator(): PrimaryRosterProposalValidator {
    return PrimaryRosterProposalValidator(
        mutableListOf<PrimaryRosterProposalRule>(
            PrimaryRosterProposalRequiredNameRule("primary roster proposal needs this field"),
            PrimaryRosterProposalRequiredCategoryRule("primary roster proposal needs this field"),
            PrimaryRosterProposalCodePrefixRule("OPS"),
            PrimaryRosterProposalLaneLengthRule(86400),
            PrimaryRosterProposalCodeLengthRule(80),
        ),
    )
}
