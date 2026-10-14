package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a hub proposal must pass. Check returns its problems, or none. */
interface HubProposalRule {
    fun check(item: HubProposal): MutableList<String>
}

data class HubProposalNotesLengthRule(var maxLength: Int) : HubProposalRule {
    override fun check(item: HubProposal): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubProposalRequiredNotesRule(var message: String) : HubProposalRule {
    override fun check(item: HubProposal): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubProposalOdometerRangeRule(var min: Int, var max: Int) : HubProposalRule {
    override fun check(item: HubProposal): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubProposalRequiredNameRule(var message: String) : HubProposalRule {
    override fun check(item: HubProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubProposalAllowedNotesRule(var allowed: MutableList<String>) : HubProposalRule {
    override fun check(item: HubProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class HubProposalNameLengthRule(var maxLength: Int) : HubProposalRule {
    override fun check(item: HubProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub proposal. */
data class HubProposalValidator(var rules: MutableList<HubProposalRule>) {
    fun validate(item: HubProposal): MutableList<String> {
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

    fun requireValid(item: HubProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultHubProposalValidator(): HubProposalValidator {
    return HubProposalValidator(
        mutableListOf<HubProposalRule>(
            HubProposalNotesLengthRule(1000),
            HubProposalRequiredNotesRule("hub proposal needs this field"),
            HubProposalOdometerRangeRule(255, 1000),
            HubProposalRequiredNameRule("hub proposal needs this field"),
            HubProposalAllowedNotesRule(mutableListOf<String>()),
            HubProposalNameLengthRule(1000),
        ),
    )
}
