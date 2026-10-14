package com.acme.transport.linehaul.tender.proposal

/** A rule a batched tender estimate must pass. Check returns its problems, or none. */
interface BatchedTenderEstimateRule {
    fun check(item: BatchedTenderEstimate): MutableList<String>
}

data class BatchedTenderEstimateRequiredCodeRule(var message: String) : BatchedTenderEstimateRule {
    override fun check(item: BatchedTenderEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderEstimateRequiredChecksumRule(var message: String) : BatchedTenderEstimateRule {
    override fun check(item: BatchedTenderEstimate): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderEstimateChecksumLengthRule(var maxLength: Int) : BatchedTenderEstimateRule {
    override fun check(item: BatchedTenderEstimate): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderEstimateArchivedGuardRule(var limit: Double) : BatchedTenderEstimateRule {
    override fun check(item: BatchedTenderEstimate): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTenderEstimateTitleLengthRule(var maxLength: Int) : BatchedTenderEstimateRule {
    override fun check(item: BatchedTenderEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tender estimate. */
data class BatchedTenderEstimateValidator(var rules: MutableList<BatchedTenderEstimateRule>) {
    fun validate(item: BatchedTenderEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTenderEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTenderEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTenderEstimateValidator(): BatchedTenderEstimateValidator {
    return BatchedTenderEstimateValidator(
        mutableListOf<BatchedTenderEstimateRule>(
            BatchedTenderEstimateRequiredCodeRule("batched tender estimate needs this field"),
            BatchedTenderEstimateRequiredChecksumRule("batched tender estimate needs this field"),
            BatchedTenderEstimateChecksumLengthRule(64),
            BatchedTenderEstimateArchivedGuardRule(8.6),
            BatchedTenderEstimateTitleLengthRule(86400),
        ),
    )
}
