package com.acme.transport.crewing.restbreak.rollup

/** A rule a batched rest break estimate must pass. Check returns its problems, or none. */
interface BatchedRestBreakEstimateRule {
    fun check(item: BatchedRestBreakEstimate): MutableList<String>
}

data class BatchedRestBreakEstimateTitleLengthRule(var maxLength: Int) : BatchedRestBreakEstimateRule {
    override fun check(item: BatchedRestBreakEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakEstimateRequiredCodeRule(var message: String) : BatchedRestBreakEstimateRule {
    override fun check(item: BatchedRestBreakEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakEstimateWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : BatchedRestBreakEstimateRule {
    override fun check(item: BatchedRestBreakEstimate): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRestBreakEstimatePinnedGuardRule(var limit: Int) : BatchedRestBreakEstimateRule {
    override fun check(item: BatchedRestBreakEstimate): MutableList<String> {
        if (item.pinned && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched rest break estimate. */
data class BatchedRestBreakEstimateValidator(var rules: MutableList<BatchedRestBreakEstimateRule>) {
    fun validate(item: BatchedRestBreakEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRestBreakEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRestBreakEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRestBreakEstimateValidator(): BatchedRestBreakEstimateValidator {
    return BatchedRestBreakEstimateValidator(
        mutableListOf<BatchedRestBreakEstimateRule>(
            BatchedRestBreakEstimateTitleLengthRule(80),
            BatchedRestBreakEstimateRequiredCodeRule("batched rest break estimate needs this field"),
            BatchedRestBreakEstimateWindowDaysRangeRule(120, 3600),
            BatchedRestBreakEstimatePinnedGuardRule(64),
        ),
    )
}
