package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.text.normalize

/** A rule a manual dock door proposal must pass. Check returns its problems, or none. */
interface ManualDockDoorProposalRule {
    fun check(item: ManualDockDoorProposal): MutableList<String>
}

data class ManualDockDoorProposalAllowedCodeRule(
    var allowed: MutableList<String>,
) : ManualDockDoorProposalRule {
    override fun check(item: ManualDockDoorProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorProposalActiveGuardRule(var limit: Int) : ManualDockDoorProposalRule {
    override fun check(item: ManualDockDoorProposal): MutableList<String> {
        if (item.active && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorProposalRequiredTitleRule(var message: String) : ManualDockDoorProposalRule {
    override fun check(item: ManualDockDoorProposal): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorProposalDockLengthRule(var maxLength: Int) : ManualDockDoorProposalRule {
    override fun check(item: ManualDockDoorProposal): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorProposalRequiredDockRule(var message: String) : ManualDockDoorProposalRule {
    override fun check(item: ManualDockDoorProposal): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualDockDoorProposalTitleLengthRule(var maxLength: Int) : ManualDockDoorProposalRule {
    override fun check(item: ManualDockDoorProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dock door proposal. */
data class ManualDockDoorProposalValidator(var rules: MutableList<ManualDockDoorProposalRule>) {
    fun validate(item: ManualDockDoorProposal): MutableList<String> {
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

    fun requireValid(item: ManualDockDoorProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDockDoorProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDockDoorProposalValidator(): ManualDockDoorProposalValidator {
    return ManualDockDoorProposalValidator(
        mutableListOf<ManualDockDoorProposalRule>(
            ManualDockDoorProposalAllowedCodeRule(mutableListOf<String>()),
            ManualDockDoorProposalActiveGuardRule(64),
            ManualDockDoorProposalRequiredTitleRule("manual dock door proposal needs an entry"),
            ManualDockDoorProposalDockLengthRule(3600),
            ManualDockDoorProposalRequiredDockRule("manual dock door proposal needs this field"),
            ManualDockDoorProposalTitleLengthRule(80),
        ),
    )
}
