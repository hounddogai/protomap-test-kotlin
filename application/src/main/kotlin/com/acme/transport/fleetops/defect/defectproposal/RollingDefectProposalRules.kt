package com.acme.transport.fleetops.defect.defectproposal

/** A rule a rolling defect proposal must pass. Check returns its problems, or none. */
interface RollingDefectProposalRule {
    fun check(item: RollingDefectProposal): MutableList<String>
}

data class RollingDefectProposalCodePrefixRule(var prefix: String) : RollingDefectProposalRule {
    override fun check(item: RollingDefectProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectProposalAmountRangeRule(var min: Double, var max: Double) : RollingDefectProposalRule {
    override fun check(item: RollingDefectProposal): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectProposalRequiredCodeRule(var message: String) : RollingDefectProposalRule {
    override fun check(item: RollingDefectProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling defect proposal. */
data class RollingDefectProposalValidator(var rules: MutableList<RollingDefectProposalRule>) {
    fun validate(item: RollingDefectProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDefectProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDefectProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDefectProposalValidator(): RollingDefectProposalValidator {
    return RollingDefectProposalValidator(
        mutableListOf<RollingDefectProposalRule>(
            RollingDefectProposalCodePrefixRule("OPS"),
            RollingDefectProposalAmountRangeRule(4.8, 4106.8),
            RollingDefectProposalRequiredCodeRule("rolling defect proposal needs a value"),
        ),
    )
}
