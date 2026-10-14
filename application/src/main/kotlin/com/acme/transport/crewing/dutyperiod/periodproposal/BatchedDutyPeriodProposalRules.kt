package com.acme.transport.crewing.dutyperiod.periodproposal

/** A rule a batched duty period proposal must pass. Check returns its problems, or none. */
interface BatchedDutyPeriodProposalRule {
    fun check(item: BatchedDutyPeriodProposal): MutableList<String>
}

data class BatchedDutyPeriodProposalRequiredShiftCodeRule(var message: String) : BatchedDutyPeriodProposalRule {
    override fun check(item: BatchedDutyPeriodProposal): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodProposalOdometerRangeRule(
    var min: Int,
    var max: Int,
) : BatchedDutyPeriodProposalRule {
    override fun check(item: BatchedDutyPeriodProposal): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class BatchedDutyPeriodProposalGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : BatchedDutyPeriodProposalRule {
    override fun check(item: BatchedDutyPeriodProposal): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class BatchedDutyPeriodProposalLabelLengthRule(var maxLength: Int) : BatchedDutyPeriodProposalRule {
    override fun check(item: BatchedDutyPeriodProposal): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodProposalCodeLengthRule(var maxLength: Int) : BatchedDutyPeriodProposalRule {
    override fun check(item: BatchedDutyPeriodProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched duty period proposal. */
data class BatchedDutyPeriodProposalValidator(var rules: MutableList<BatchedDutyPeriodProposalRule>) {
    fun validate(item: BatchedDutyPeriodProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedDutyPeriodProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDutyPeriodProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDutyPeriodProposalValidator(): BatchedDutyPeriodProposalValidator {
    return BatchedDutyPeriodProposalValidator(
        mutableListOf<BatchedDutyPeriodProposalRule>(
            BatchedDutyPeriodProposalRequiredShiftCodeRule("batched duty period proposal needs this field"),
            BatchedDutyPeriodProposalOdometerRangeRule(255, 255),
            BatchedDutyPeriodProposalGraceDaysRangeRule(1000, 86400),
            BatchedDutyPeriodProposalLabelLengthRule(64),
            BatchedDutyPeriodProposalCodeLengthRule(16),
        ),
    )
}
