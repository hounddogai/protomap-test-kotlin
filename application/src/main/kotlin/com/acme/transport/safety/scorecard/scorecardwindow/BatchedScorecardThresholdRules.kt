package com.acme.transport.safety.scorecard.scorecardwindow

/** A rule a batched scorecard threshold must pass. Check returns its problems, or none. */
interface BatchedScorecardThresholdRule {
    fun check(item: BatchedScorecardThreshold): MutableList<String>
}

data class BatchedScorecardThresholdCodeLengthRule(var maxLength: Int) : BatchedScorecardThresholdRule {
    override fun check(item: BatchedScorecardThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardThresholdShiftCodeLengthRule(var maxLength: Int) : BatchedScorecardThresholdRule {
    override fun check(item: BatchedScorecardThreshold): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardThresholdCodePrefixRule(var prefix: String) : BatchedScorecardThresholdRule {
    override fun check(item: BatchedScorecardThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedScorecardThresholdMarginRangeRule(
    var min: Double,
    var max: Double,
) : BatchedScorecardThresholdRule {
    override fun check(item: BatchedScorecardThreshold): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class BatchedScorecardThresholdRequiredCodeRule(var message: String) : BatchedScorecardThresholdRule {
    override fun check(item: BatchedScorecardThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched scorecard threshold. */
data class BatchedScorecardThresholdValidator(var rules: MutableList<BatchedScorecardThresholdRule>) {
    fun validate(item: BatchedScorecardThreshold): MutableList<String> {
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

    fun requireValid(item: BatchedScorecardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedScorecardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedScorecardThresholdValidator(): BatchedScorecardThresholdValidator {
    return BatchedScorecardThresholdValidator(
        mutableListOf<BatchedScorecardThresholdRule>(
            BatchedScorecardThresholdCodeLengthRule(64),
            BatchedScorecardThresholdShiftCodeLengthRule(80),
            BatchedScorecardThresholdCodePrefixRule("Q"),
            BatchedScorecardThresholdMarginRangeRule(2.2, 2316.0),
            BatchedScorecardThresholdRequiredCodeRule("batched scorecard threshold needs a value"),
        ),
    )
}
