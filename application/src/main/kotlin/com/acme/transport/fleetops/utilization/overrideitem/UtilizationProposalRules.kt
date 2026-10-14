package com.acme.transport.fleetops.utilization.overrideitem

/** A rule a utilization proposal must pass. Check returns its problems, or none. */
interface UtilizationProposalRule {
    fun check(item: UtilizationProposal): MutableList<String>
}

data class UtilizationProposalCodePrefixRule(var prefix: String) : UtilizationProposalRule {
    override fun check(item: UtilizationProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationProposalRequiredNameRule(var message: String) : UtilizationProposalRule {
    override fun check(item: UtilizationProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationProposalNameLengthRule(var maxLength: Int) : UtilizationProposalRule {
    override fun check(item: UtilizationProposal): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationProposalDepotLengthRule(var maxLength: Int) : UtilizationProposalRule {
    override fun check(item: UtilizationProposal): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization proposal. */
data class UtilizationProposalValidator(var rules: MutableList<UtilizationProposalRule>) {
    fun validate(item: UtilizationProposal): MutableList<String> {
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

    fun requireValid(item: UtilizationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationProposalValidator(): UtilizationProposalValidator {
    return UtilizationProposalValidator(
        mutableListOf<UtilizationProposalRule>(
            UtilizationProposalCodePrefixRule("Q"),
            UtilizationProposalRequiredNameRule("utilization proposal needs this field"),
            UtilizationProposalNameLengthRule(80),
            UtilizationProposalDepotLengthRule(1000),
        ),
    )
}
