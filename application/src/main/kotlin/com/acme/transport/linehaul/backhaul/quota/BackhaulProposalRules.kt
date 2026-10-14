package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.text.normalize

/** A rule a backhaul proposal must pass. Check returns its problems, or none. */
interface BackhaulProposalRule {
    fun check(item: BackhaulProposal): MutableList<String>
}

data class BackhaulProposalLabelLengthRule(var maxLength: Int) : BackhaulProposalRule {
    override fun check(item: BackhaulProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProposalCostRangeRule(var min: Double, var max: Double) : BackhaulProposalRule {
    override fun check(item: BackhaulProposal): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProposalAllowedNotesRule(var allowed: MutableList<String>) : BackhaulProposalRule {
    override fun check(item: BackhaulProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProposalRequiredNameRule(var message: String) : BackhaulProposalRule {
    override fun check(item: BackhaulProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProposalExpiresAtRangeRule(var min: Int, var max: Int) : BackhaulProposalRule {
    override fun check(item: BackhaulProposal): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulProposalVisibleGuardRule(var limit: Double) : BackhaulProposalRule {
    override fun check(item: BackhaulProposal): MutableList<String> {
        if (item.visible && item.cost > limit) {
            return mutableListOf<String>("cost ${item.cost} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul proposal. */
data class BackhaulProposalValidator(var rules: MutableList<BackhaulProposalRule>) {
    fun validate(item: BackhaulProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulProposalValidator(): BackhaulProposalValidator {
    return BackhaulProposalValidator(
        mutableListOf<BackhaulProposalRule>(
            BackhaulProposalLabelLengthRule(3600),
            BackhaulProposalCostRangeRule(4.8, 4118.0),
            BackhaulProposalAllowedNotesRule(mutableListOf<String>()),
            BackhaulProposalRequiredNameRule("backhaul proposal needs a value"),
            BackhaulProposalExpiresAtRangeRule(64, 80),
            BackhaulProposalVisibleGuardRule(3.8),
        ),
    )
}
