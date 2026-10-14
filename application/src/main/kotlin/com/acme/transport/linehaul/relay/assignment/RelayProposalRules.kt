package com.acme.transport.linehaul.relay.assignment

/** A rule a relay proposal must pass. Check returns its problems, or none. */
interface RelayProposalRule {
    fun check(item: RelayProposal): MutableList<String>
}

data class RelayProposalRequiredRegionRule(var message: String) : RelayProposalRule {
    override fun check(item: RelayProposal): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayProposalRegionLengthRule(var maxLength: Int) : RelayProposalRule {
    override fun check(item: RelayProposal): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayProposalReviewerLengthRule(var maxLength: Int) : RelayProposalRule {
    override fun check(item: RelayProposal): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayProposalWindowDaysRangeRule(var min: Int, var max: Int) : RelayProposalRule {
    override fun check(item: RelayProposal): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RelayProposalCodeLengthRule(var maxLength: Int) : RelayProposalRule {
    override fun check(item: RelayProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayProposalOdometerRangeRule(var min: Int, var max: Int) : RelayProposalRule {
    override fun check(item: RelayProposal): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay proposal. */
data class RelayProposalValidator(var rules: MutableList<RelayProposalRule>) {
    fun validate(item: RelayProposal): MutableList<String> {
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

    fun requireValid(item: RelayProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayProposalValidator(): RelayProposalValidator {
    return RelayProposalValidator(
        mutableListOf<RelayProposalRule>(
            RelayProposalRequiredRegionRule("relay proposal needs this field"),
            RelayProposalRegionLengthRule(86400),
            RelayProposalReviewerLengthRule(86400),
            RelayProposalWindowDaysRangeRule(80, 86400),
            RelayProposalCodeLengthRule(32),
            RelayProposalOdometerRangeRule(16, 120),
        ),
    )
}
