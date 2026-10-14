package com.acme.transport.emissions.emissionfactor.manifest

/** A rule a emission factor proposal must pass. Check returns its problems, or none. */
interface EmissionFactorProposalRule {
    fun check(item: EmissionFactorProposal): MutableList<String>
}

data class EmissionFactorProposalTitleLengthRule(var maxLength: Int) : EmissionFactorProposalRule {
    override fun check(item: EmissionFactorProposal): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorProposalCodeLengthRule(var maxLength: Int) : EmissionFactorProposalRule {
    override fun check(item: EmissionFactorProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorProposalRequiredTimezoneRule(var message: String) : EmissionFactorProposalRule {
    override fun check(item: EmissionFactorProposal): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor proposal. */
data class EmissionFactorProposalValidator(var rules: MutableList<EmissionFactorProposalRule>) {
    fun validate(item: EmissionFactorProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorProposalValidator(): EmissionFactorProposalValidator {
    return EmissionFactorProposalValidator(
        mutableListOf<EmissionFactorProposalRule>(
            EmissionFactorProposalTitleLengthRule(255),
            EmissionFactorProposalCodeLengthRule(32),
            EmissionFactorProposalRequiredTimezoneRule("emission factor proposal needs an entry"),
        ),
    )
}
