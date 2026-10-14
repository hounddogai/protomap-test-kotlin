package com.acme.transport.safety.incident.globalincidentproposal

/** A rule a default incident proposal must pass. Check returns its problems, or none. */
interface DefaultIncidentProposalRule {
    fun check(item: DefaultIncidentProposal): MutableList<String>
}

data class DefaultIncidentProposalAttemptsRangeRule(var min: Int, var max: Int) : DefaultIncidentProposalRule {
    override fun check(item: DefaultIncidentProposal): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class DefaultIncidentProposalPalletsRangeRule(var min: Int, var max: Int) : DefaultIncidentProposalRule {
    override fun check(item: DefaultIncidentProposal): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class DefaultIncidentProposalLabelLengthRule(var maxLength: Int) : DefaultIncidentProposalRule {
    override fun check(item: DefaultIncidentProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIncidentProposalStopCountRangeRule(var min: Int, var max: Int) : DefaultIncidentProposalRule {
    override fun check(item: DefaultIncidentProposal): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIncidentProposalRequiredCodeRule(var message: String) : DefaultIncidentProposalRule {
    override fun check(item: DefaultIncidentProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultIncidentProposalCodePrefixRule(var prefix: String) : DefaultIncidentProposalRule {
    override fun check(item: DefaultIncidentProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default incident proposal. */
data class DefaultIncidentProposalValidator(var rules: MutableList<DefaultIncidentProposalRule>) {
    fun validate(item: DefaultIncidentProposal): MutableList<String> {
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

    fun requireValid(item: DefaultIncidentProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultIncidentProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultIncidentProposalValidator(): DefaultIncidentProposalValidator {
    return DefaultIncidentProposalValidator(
        mutableListOf<DefaultIncidentProposalRule>(
            DefaultIncidentProposalAttemptsRangeRule(16, 64),
            DefaultIncidentProposalPalletsRangeRule(86400, 86400),
            DefaultIncidentProposalLabelLengthRule(64),
            DefaultIncidentProposalStopCountRangeRule(64, 86400),
            DefaultIncidentProposalRequiredCodeRule("default incident proposal needs an entry"),
            DefaultIncidentProposalCodePrefixRule("X"),
        ),
    )
}
