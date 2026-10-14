package com.acme.transport.safety.incident.incidentsnapshot

/** A rule a adaptive incident proposal must pass. Check returns its problems, or none. */
interface AdaptiveIncidentProposalRule {
    fun check(item: AdaptiveIncidentProposal): MutableList<String>
}

data class AdaptiveIncidentProposalLabelLengthRule(var maxLength: Int) : AdaptiveIncidentProposalRule {
    override fun check(item: AdaptiveIncidentProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentProposalThresholdRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveIncidentProposalRule {
    override fun check(item: AdaptiveIncidentProposal): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveIncidentProposalCodeLengthRule(var maxLength: Int) : AdaptiveIncidentProposalRule {
    override fun check(item: AdaptiveIncidentProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive incident proposal. */
data class AdaptiveIncidentProposalValidator(var rules: MutableList<AdaptiveIncidentProposalRule>) {
    fun validate(item: AdaptiveIncidentProposal): MutableList<String> {
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

    fun requireValid(item: AdaptiveIncidentProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveIncidentProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveIncidentProposalValidator(): AdaptiveIncidentProposalValidator {
    return AdaptiveIncidentProposalValidator(
        mutableListOf<AdaptiveIncidentProposalRule>(
            AdaptiveIncidentProposalLabelLengthRule(86400),
            AdaptiveIncidentProposalThresholdRangeRule(0.3, 2134.3),
            AdaptiveIncidentProposalCodeLengthRule(3600),
        ),
    )
}
