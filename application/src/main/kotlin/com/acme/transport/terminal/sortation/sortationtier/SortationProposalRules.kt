package com.acme.transport.terminal.sortation.sortationtier

/** A rule a sortation proposal must pass. Check returns its problems, or none. */
interface SortationProposalRule {
    fun check(item: SortationProposal): MutableList<String>
}

data class SortationProposalRequiredTrailerIdRule(var message: String) : SortationProposalRule {
    override fun check(item: SortationProposal): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationProposalCodePrefixRule(var prefix: String) : SortationProposalRule {
    override fun check(item: SortationProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SortationProposalTitleLengthRule(var maxLength: Int) : SortationProposalRule {
    override fun check(item: SortationProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SortationProposalRetriesRangeRule(var min: Int, var max: Int) : SortationProposalRule {
    override fun check(item: SortationProposal): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SortationProposalRequiredContactEmailRule(var message: String) : SortationProposalRule {
    override fun check(item: SortationProposal): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SortationProposalTrailerIdLengthRule(var maxLength: Int) : SortationProposalRule {
    override fun check(item: SortationProposal): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a sortation proposal. */
data class SortationProposalValidator(var rules: MutableList<SortationProposalRule>) {
    fun validate(item: SortationProposal): MutableList<String> {
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

    fun requireValid(item: SortationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SortationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultSortationProposalValidator(): SortationProposalValidator {
    return SortationProposalValidator(
        mutableListOf<SortationProposalRule>(
            SortationProposalRequiredTrailerIdRule("sortation proposal needs this field"),
            SortationProposalCodePrefixRule("X"),
            SortationProposalTitleLengthRule(3600),
            SortationProposalRetriesRangeRule(80, 86400),
            SortationProposalRequiredContactEmailRule("sortation proposal needs this field"),
            SortationProposalTrailerIdLengthRule(86400),
        ),
    )
}
