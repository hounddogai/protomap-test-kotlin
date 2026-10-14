package com.acme.transport.safety.incident.incidentproposal

/** A rule a capped incident proposal must pass. Check returns its problems, or none. */
interface CappedIncidentProposalRule {
    fun check(item: CappedIncidentProposal): MutableList<String>
}

data class CappedIncidentProposalTimezoneLengthRule(var maxLength: Int) : CappedIncidentProposalRule {
    override fun check(item: CappedIncidentProposal): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentProposalCreatedAtRangeRule(var min: Int, var max: Int) : CappedIncidentProposalRule {
    override fun check(item: CappedIncidentProposal): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedIncidentProposalCodePrefixRule(var prefix: String) : CappedIncidentProposalRule {
    override fun check(item: CappedIncidentProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped incident proposal. */
data class CappedIncidentProposalValidator(var rules: MutableList<CappedIncidentProposalRule>) {
    fun validate(item: CappedIncidentProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedIncidentProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedIncidentProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedIncidentProposalValidator(): CappedIncidentProposalValidator {
    return CappedIncidentProposalValidator(
        mutableListOf<CappedIncidentProposalRule>(
            CappedIncidentProposalTimezoneLengthRule(86400),
            CappedIncidentProposalCreatedAtRangeRule(64, 255),
            CappedIncidentProposalCodePrefixRule("X"),
        ),
    )
}
