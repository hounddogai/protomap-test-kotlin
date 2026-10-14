package com.acme.transport.emissions.idling.idlingforecast

/** A rule a batched idling baseline must pass. Check returns its problems, or none. */
interface BatchedIdlingBaselineRule {
    fun check(item: BatchedIdlingBaseline): MutableList<String>
}

data class BatchedIdlingBaselineRequiredLaneRule(var message: String) : BatchedIdlingBaselineRule {
    override fun check(item: BatchedIdlingBaseline): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingBaselineRequiredTimezoneRule(var message: String) : BatchedIdlingBaselineRule {
    override fun check(item: BatchedIdlingBaseline): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingBaselineBatchSizeRangeRule(var min: Int, var max: Int) : BatchedIdlingBaselineRule {
    override fun check(item: BatchedIdlingBaseline): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingBaselineTitleLengthRule(var maxLength: Int) : BatchedIdlingBaselineRule {
    override fun check(item: BatchedIdlingBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingBaselineWindowDaysRangeRule(var min: Int, var max: Int) : BatchedIdlingBaselineRule {
    override fun check(item: BatchedIdlingBaseline): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedIdlingBaselineTimezoneLengthRule(var maxLength: Int) : BatchedIdlingBaselineRule {
    override fun check(item: BatchedIdlingBaseline): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched idling baseline. */
data class BatchedIdlingBaselineValidator(var rules: MutableList<BatchedIdlingBaselineRule>) {
    fun validate(item: BatchedIdlingBaseline): MutableList<String> {
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

    fun requireValid(item: BatchedIdlingBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedIdlingBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedIdlingBaselineValidator(): BatchedIdlingBaselineValidator {
    return BatchedIdlingBaselineValidator(
        mutableListOf<BatchedIdlingBaselineRule>(
            BatchedIdlingBaselineRequiredLaneRule("batched idling baseline needs an entry"),
            BatchedIdlingBaselineRequiredTimezoneRule("batched idling baseline needs this field"),
            BatchedIdlingBaselineBatchSizeRangeRule(16, 120),
            BatchedIdlingBaselineTitleLengthRule(64),
            BatchedIdlingBaselineWindowDaysRangeRule(16, 86400),
            BatchedIdlingBaselineTimezoneLengthRule(16),
        ),
    )
}
