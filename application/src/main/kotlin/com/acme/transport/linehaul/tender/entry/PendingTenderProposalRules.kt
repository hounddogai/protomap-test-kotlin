package com.acme.transport.linehaul.tender.entry

/** A rule a pending tender proposal must pass. Check returns its problems, or none. */
interface PendingTenderProposalRule {
    fun check(item: PendingTenderProposal): MutableList<String>
}

data class PendingTenderProposalRequiredPhoneRule(var message: String) : PendingTenderProposalRule {
    override fun check(item: PendingTenderProposal): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderProposalRequiredCountryRule(var message: String) : PendingTenderProposalRule {
    override fun check(item: PendingTenderProposal): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderProposalNameLengthRule(var maxLength: Int) : PendingTenderProposalRule {
    override fun check(item: PendingTenderProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderProposalCountryLengthRule(var maxLength: Int) : PendingTenderProposalRule {
    override fun check(item: PendingTenderProposal): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderProposalRequiredCodeRule(var message: String) : PendingTenderProposalRule {
    override fun check(item: PendingTenderProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderProposalThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PendingTenderProposalRule {
    override fun check(item: PendingTenderProposal): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tender proposal. */
data class PendingTenderProposalValidator(var rules: MutableList<PendingTenderProposalRule>) {
    fun validate(item: PendingTenderProposal): MutableList<String> {
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

    fun requireValid(item: PendingTenderProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTenderProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTenderProposalValidator(): PendingTenderProposalValidator {
    return PendingTenderProposalValidator(
        mutableListOf<PendingTenderProposalRule>(
            PendingTenderProposalRequiredPhoneRule("pending tender proposal needs an entry"),
            PendingTenderProposalRequiredCountryRule("pending tender proposal needs an entry"),
            PendingTenderProposalNameLengthRule(64),
            PendingTenderProposalCountryLengthRule(16),
            PendingTenderProposalRequiredCodeRule("pending tender proposal needs this field"),
            PendingTenderProposalThresholdRangeRule(8.8, 4378.5),
        ),
    )
}
