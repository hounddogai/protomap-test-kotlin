package com.acme.transport.emissions.emissionfactor.factorproposal

/** A rule a capped emission factor proposal must pass. Check returns its problems, or none. */
interface CappedEmissionFactorProposalRule {
    fun check(item: CappedEmissionFactorProposal): MutableList<String>
}

data class CappedEmissionFactorProposalApprovedGuardRule(var limit: Double) : CappedEmissionFactorProposalRule {
    override fun check(item: CappedEmissionFactorProposal): MutableList<String> {
        if (item.approved && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CappedEmissionFactorProposalCeilingRangeRule(
    var min: Double,
    var max: Double,
) : CappedEmissionFactorProposalRule {
    override fun check(item: CappedEmissionFactorProposal): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class CappedEmissionFactorProposalDockLengthRule(var maxLength: Int) : CappedEmissionFactorProposalRule {
    override fun check(item: CappedEmissionFactorProposal): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedEmissionFactorProposalCodeLengthRule(var maxLength: Int) : CappedEmissionFactorProposalRule {
    override fun check(item: CappedEmissionFactorProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped emission factor proposal. */
data class CappedEmissionFactorProposalValidator(var rules: MutableList<CappedEmissionFactorProposalRule>) {
    fun validate(item: CappedEmissionFactorProposal): MutableList<String> {
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

    fun requireValid(item: CappedEmissionFactorProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedEmissionFactorProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedEmissionFactorProposalValidator(): CappedEmissionFactorProposalValidator {
    return CappedEmissionFactorProposalValidator(
        mutableListOf<CappedEmissionFactorProposalRule>(
            CappedEmissionFactorProposalApprovedGuardRule(1.9),
            CappedEmissionFactorProposalCeilingRangeRule(3.8, 1912.5),
            CappedEmissionFactorProposalDockLengthRule(120),
            CappedEmissionFactorProposalCodeLengthRule(86400),
        ),
    )
}
