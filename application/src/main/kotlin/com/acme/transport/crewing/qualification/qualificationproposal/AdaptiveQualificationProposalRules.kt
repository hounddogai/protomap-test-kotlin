package com.acme.transport.crewing.qualification.qualificationproposal

/** A rule a adaptive qualification proposal must pass. Check returns its problems, or none. */
interface AdaptiveQualificationProposalRule {
    fun check(item: AdaptiveQualificationProposal): MutableList<String>
}

data class AdaptiveQualificationProposalShiftCodeLengthRule(
    var maxLength: Int,
) : AdaptiveQualificationProposalRule {
    override fun check(item: AdaptiveQualificationProposal): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveQualificationProposalAmountRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveQualificationProposalRule {
    override fun check(item: AdaptiveQualificationProposal): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class AdaptiveQualificationProposalLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveQualificationProposalRule {
    override fun check(item: AdaptiveQualificationProposal): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive qualification proposal. */
data class AdaptiveQualificationProposalValidator(var rules: MutableList<AdaptiveQualificationProposalRule>) {
    fun validate(item: AdaptiveQualificationProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveQualificationProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveQualificationProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveQualificationProposalValidator(): AdaptiveQualificationProposalValidator {
    return AdaptiveQualificationProposalValidator(
        mutableListOf<AdaptiveQualificationProposalRule>(
            AdaptiveQualificationProposalShiftCodeLengthRule(120),
            AdaptiveQualificationProposalAmountRangeRule(0.4, 708.1),
            AdaptiveQualificationProposalLoadKgRangeRule(2.4, 4758.1),
        ),
    )
}
