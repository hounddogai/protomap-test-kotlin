package com.acme.transport.linehaul.capacity.capacitycycle

/** A rule a scheduled capacity proposal must pass. Check returns its problems, or none. */
interface ScheduledCapacityProposalRule {
    fun check(item: ScheduledCapacityProposal): MutableList<String>
}

data class ScheduledCapacityProposalRequiredCodeRule(var message: String) : ScheduledCapacityProposalRule {
    override fun check(item: ScheduledCapacityProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityProposalSourceLengthRule(var maxLength: Int) : ScheduledCapacityProposalRule {
    override fun check(item: ScheduledCapacityProposal): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityProposalCodePrefixRule(var prefix: String) : ScheduledCapacityProposalRule {
    override fun check(item: ScheduledCapacityProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityProposalSequenceRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledCapacityProposalRule {
    override fun check(item: ScheduledCapacityProposal): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class ScheduledCapacityProposalRequiredSourceRule(var message: String) : ScheduledCapacityProposalRule {
    override fun check(item: ScheduledCapacityProposal): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledCapacityProposalRequiredLabelRule(var message: String) : ScheduledCapacityProposalRule {
    override fun check(item: ScheduledCapacityProposal): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled capacity proposal. */
data class ScheduledCapacityProposalValidator(var rules: MutableList<ScheduledCapacityProposalRule>) {
    fun validate(item: ScheduledCapacityProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledCapacityProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledCapacityProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledCapacityProposalValidator(): ScheduledCapacityProposalValidator {
    return ScheduledCapacityProposalValidator(
        mutableListOf<ScheduledCapacityProposalRule>(
            ScheduledCapacityProposalRequiredCodeRule("scheduled capacity proposal needs a value"),
            ScheduledCapacityProposalSourceLengthRule(32),
            ScheduledCapacityProposalCodePrefixRule("ACM"),
            ScheduledCapacityProposalSequenceRangeRule(80, 120),
            ScheduledCapacityProposalRequiredSourceRule("scheduled capacity proposal needs a value"),
            ScheduledCapacityProposalRequiredLabelRule("scheduled capacity proposal needs a value"),
        ),
    )
}
