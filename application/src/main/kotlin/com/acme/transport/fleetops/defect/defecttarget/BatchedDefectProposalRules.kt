package com.acme.transport.fleetops.defect.defecttarget

/** A rule a batched defect proposal must pass. Check returns its problems, or none. */
interface BatchedDefectProposalRule {
    fun check(item: BatchedDefectProposal): MutableList<String>
}

data class BatchedDefectProposalEtaMinutesRangeRule(var min: Int, var max: Int) : BatchedDefectProposalRule {
    override fun check(item: BatchedDefectProposal): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class BatchedDefectProposalCodeLengthRule(var maxLength: Int) : BatchedDefectProposalRule {
    override fun check(item: BatchedDefectProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectProposalRequiredNameRule(var message: String) : BatchedDefectProposalRule {
    override fun check(item: BatchedDefectProposal): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectProposalRequiredCodeRule(var message: String) : BatchedDefectProposalRule {
    override fun check(item: BatchedDefectProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDefectProposalThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDefectProposalRule {
    override fun check(item: BatchedDefectProposal): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched defect proposal. */
data class BatchedDefectProposalValidator(var rules: MutableList<BatchedDefectProposalRule>) {
    fun validate(item: BatchedDefectProposal): MutableList<String> {
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

    fun requireValid(item: BatchedDefectProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDefectProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDefectProposalValidator(): BatchedDefectProposalValidator {
    return BatchedDefectProposalValidator(
        mutableListOf<BatchedDefectProposalRule>(
            BatchedDefectProposalEtaMinutesRangeRule(16, 80),
            BatchedDefectProposalCodeLengthRule(80),
            BatchedDefectProposalRequiredNameRule("batched defect proposal needs a value"),
            BatchedDefectProposalRequiredCodeRule("batched defect proposal needs a value"),
            BatchedDefectProposalThresholdRangeRule(5.1, 1816.1),
        ),
    )
}
