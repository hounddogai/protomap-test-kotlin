package com.acme.transport.crewing.dutyperiod.proposal

/** A rule a duty period proposal must pass. Check returns its problems, or none. */
interface DutyPeriodProposalRule {
    fun check(item: DutyPeriodProposal): MutableList<String>
}

data class DutyPeriodProposalAmountRangeRule(var min: Double, var max: Double) : DutyPeriodProposalRule {
    override fun check(item: DutyPeriodProposal): MutableList<String> {
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

data class DutyPeriodProposalRequiredCategoryRule(var message: String) : DutyPeriodProposalRule {
    override fun check(item: DutyPeriodProposal): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodProposalStopCountRangeRule(var min: Int, var max: Int) : DutyPeriodProposalRule {
    override fun check(item: DutyPeriodProposal): MutableList<String> {
        val value = item.stopCount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("stop count $value is below $min")
        }
        if (value > max) {
            problems.add("stop count $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a duty period proposal. */
data class DutyPeriodProposalValidator(var rules: MutableList<DutyPeriodProposalRule>) {
    fun validate(item: DutyPeriodProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DutyPeriodProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodProposalValidator(): DutyPeriodProposalValidator {
    return DutyPeriodProposalValidator(
        mutableListOf<DutyPeriodProposalRule>(
            DutyPeriodProposalAmountRangeRule(6.4, 2020.6),
            DutyPeriodProposalRequiredCategoryRule("duty period proposal needs a value"),
            DutyPeriodProposalStopCountRangeRule(255, 86400),
        ),
    )
}
