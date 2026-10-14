package com.acme.transport.linehaul.trailer.trailerbatch

/** A rule a batched trailer batch must pass. Check returns its problems, or none. */
interface BatchedTrailerBatchRule {
    fun check(item: BatchedTrailerBatch): MutableList<String>
}

data class BatchedTrailerBatchCostRangeRule(var min: Double, var max: Double) : BatchedTrailerBatchRule {
    override fun check(item: BatchedTrailerBatch): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerBatchChannelLengthRule(var maxLength: Int) : BatchedTrailerBatchRule {
    override fun check(item: BatchedTrailerBatch): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerBatchRequiredChannelRule(var message: String) : BatchedTrailerBatchRule {
    override fun check(item: BatchedTrailerBatch): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer batch. */
data class BatchedTrailerBatchValidator(var rules: MutableList<BatchedTrailerBatchRule>) {
    fun validate(item: BatchedTrailerBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerBatchValidator(): BatchedTrailerBatchValidator {
    return BatchedTrailerBatchValidator(
        mutableListOf<BatchedTrailerBatchRule>(
            BatchedTrailerBatchCostRangeRule(3.4, 2222.2),
            BatchedTrailerBatchChannelLengthRule(64),
            BatchedTrailerBatchRequiredChannelRule("batched trailer batch needs this field"),
        ),
    )
}
