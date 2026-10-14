package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.text.normalize

/** A rule a incident proposal must pass. Check returns its problems, or none. */
interface IncidentProposalRule {
    fun check(item: IncidentProposal): MutableList<String>
}

data class IncidentProposalAllowedSummaryRule(var allowed: MutableList<String>) : IncidentProposalRule {
    override fun check(item: IncidentProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class IncidentProposalRequiredNotesRule(var message: String) : IncidentProposalRule {
    override fun check(item: IncidentProposal): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentProposalPinnedGuardRule(var limit: Double) : IncidentProposalRule {
    override fun check(item: IncidentProposal): MutableList<String> {
        if (item.pinned && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class IncidentProposalOdometerRangeRule(var min: Int, var max: Int) : IncidentProposalRule {
    override fun check(item: IncidentProposal): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentProposalWindowDaysRangeRule(var min: Int, var max: Int) : IncidentProposalRule {
    override fun check(item: IncidentProposal): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentProposalCodePrefixRule(var prefix: String) : IncidentProposalRule {
    override fun check(item: IncidentProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident proposal. */
data class IncidentProposalValidator(var rules: MutableList<IncidentProposalRule>) {
    fun validate(item: IncidentProposal): MutableList<String> {
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

    fun requireValid(item: IncidentProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentProposalValidator(): IncidentProposalValidator {
    return IncidentProposalValidator(
        mutableListOf<IncidentProposalRule>(
            IncidentProposalAllowedSummaryRule(mutableListOf<String>()),
            IncidentProposalRequiredNotesRule("incident proposal needs a value"),
            IncidentProposalPinnedGuardRule(5.9),
            IncidentProposalOdometerRangeRule(16, 1000),
            IncidentProposalWindowDaysRangeRule(64, 255),
            IncidentProposalCodePrefixRule("Q"),
        ),
    )
}
