package com.acme.transport.crewing.availability.mapping

/** A rule a global availability proposal must pass. Check returns its problems, or none. */
interface GlobalAvailabilityProposalRule {
    fun check(item: GlobalAvailabilityProposal): MutableList<String>
}

data class GlobalAvailabilityProposalRetriesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalAvailabilityProposalRule {
    override fun check(item: GlobalAvailabilityProposal): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class GlobalAvailabilityProposalGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : GlobalAvailabilityProposalRule {
    override fun check(item: GlobalAvailabilityProposal): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class GlobalAvailabilityProposalRequiredLabelRule(var message: String) : GlobalAvailabilityProposalRule {
    override fun check(item: GlobalAvailabilityProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityProposalBudgetRangeRule(
    var min: Double,
    var max: Double,
) : GlobalAvailabilityProposalRule {
    override fun check(item: GlobalAvailabilityProposal): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityProposalLabelLengthRule(var maxLength: Int) : GlobalAvailabilityProposalRule {
    override fun check(item: GlobalAvailabilityProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAvailabilityProposalCodePrefixRule(var prefix: String) : GlobalAvailabilityProposalRule {
    override fun check(item: GlobalAvailabilityProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global availability proposal. */
data class GlobalAvailabilityProposalValidator(var rules: MutableList<GlobalAvailabilityProposalRule>) {
    fun validate(item: GlobalAvailabilityProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalAvailabilityProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAvailabilityProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAvailabilityProposalValidator(): GlobalAvailabilityProposalValidator {
    return GlobalAvailabilityProposalValidator(
        mutableListOf<GlobalAvailabilityProposalRule>(
            GlobalAvailabilityProposalRetriesRangeRule(64, 3600),
            GlobalAvailabilityProposalGraceDaysRangeRule(1000, 3600),
            GlobalAvailabilityProposalRequiredLabelRule("global availability proposal needs an entry"),
            GlobalAvailabilityProposalBudgetRangeRule(3.5, 3331.0),
            GlobalAvailabilityProposalLabelLengthRule(86400),
            GlobalAvailabilityProposalCodePrefixRule("Q"),
        ),
    )
}
