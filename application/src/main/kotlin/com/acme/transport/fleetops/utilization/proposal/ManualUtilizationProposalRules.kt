package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.kit.text.normalize

/** A rule a manual utilization proposal must pass. Check returns its problems, or none. */
interface ManualUtilizationProposalRule {
    fun check(item: ManualUtilizationProposal): MutableList<String>
}

data class ManualUtilizationProposalMinimumRangeRule(
    var min: Double,
    var max: Double,
) : ManualUtilizationProposalRule {
    override fun check(item: ManualUtilizationProposal): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationProposalCodePrefixRule(var prefix: String) : ManualUtilizationProposalRule {
    override fun check(item: ManualUtilizationProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationProposalAllowedSourceRule(
    var allowed: MutableList<String>,
) : ManualUtilizationProposalRule {
    override fun check(item: ManualUtilizationProposal): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationProposalRequiredNameRule(var message: String) : ManualUtilizationProposalRule {
    override fun check(item: ManualUtilizationProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationProposalBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ManualUtilizationProposalRule {
    override fun check(item: ManualUtilizationProposal): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualUtilizationProposalNameLengthRule(var maxLength: Int) : ManualUtilizationProposalRule {
    override fun check(item: ManualUtilizationProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual utilization proposal. */
data class ManualUtilizationProposalValidator(var rules: MutableList<ManualUtilizationProposalRule>) {
    fun validate(item: ManualUtilizationProposal): MutableList<String> {
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

    fun requireValid(item: ManualUtilizationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualUtilizationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultManualUtilizationProposalValidator(): ManualUtilizationProposalValidator {
    return ManualUtilizationProposalValidator(
        mutableListOf<ManualUtilizationProposalRule>(
            ManualUtilizationProposalMinimumRangeRule(3.0, 4172.3),
            ManualUtilizationProposalCodePrefixRule("Q"),
            ManualUtilizationProposalAllowedSourceRule(mutableListOf<String>("strict", "fallback")),
            ManualUtilizationProposalRequiredNameRule("manual utilization proposal needs a value"),
            ManualUtilizationProposalBudgetRangeRule(5.5, 1308.1),
            ManualUtilizationProposalNameLengthRule(120),
        ),
    )
}
