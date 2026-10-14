package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.text.normalize

/** A rule a legacy hub proposal must pass. Check returns its problems, or none. */
interface LegacyHubProposalRule {
    fun check(item: LegacyHubProposal): MutableList<String>
}

data class LegacyHubProposalRequiredCodeRule(var message: String) : LegacyHubProposalRule {
    override fun check(item: LegacyHubProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubProposalUpdatedAtRangeRule(var min: Int, var max: Int) : LegacyHubProposalRule {
    override fun check(item: LegacyHubProposal): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubProposalAllowedCodeRule(var allowed: MutableList<String>) : LegacyHubProposalRule {
    override fun check(item: LegacyHubProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class LegacyHubProposalPriorityRangeRule(var min: Int, var max: Int) : LegacyHubProposalRule {
    override fun check(item: LegacyHubProposal): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy hub proposal. */
data class LegacyHubProposalValidator(var rules: MutableList<LegacyHubProposalRule>) {
    fun validate(item: LegacyHubProposal): MutableList<String> {
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

    fun requireValid(item: LegacyHubProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyHubProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyHubProposalValidator(): LegacyHubProposalValidator {
    return LegacyHubProposalValidator(
        mutableListOf<LegacyHubProposalRule>(
            LegacyHubProposalRequiredCodeRule("legacy hub proposal needs an entry"),
            LegacyHubProposalUpdatedAtRangeRule(255, 255),
            LegacyHubProposalAllowedCodeRule(mutableListOf<String>("manual", "batched")),
            LegacyHubProposalPriorityRangeRule(1000, 1000),
        ),
    )
}
