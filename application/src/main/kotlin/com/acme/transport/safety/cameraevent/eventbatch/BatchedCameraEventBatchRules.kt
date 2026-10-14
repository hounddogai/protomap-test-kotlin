package com.acme.transport.safety.cameraevent.eventbatch

/** A rule a batched camera event batch must pass. Check returns its problems, or none. */
interface BatchedCameraEventBatchRule {
    fun check(item: BatchedCameraEventBatch): MutableList<String>
}

data class BatchedCameraEventBatchNameLengthRule(var maxLength: Int) : BatchedCameraEventBatchRule {
    override fun check(item: BatchedCameraEventBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCameraEventBatchThresholdRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCameraEventBatchRule {
    override fun check(item: BatchedCameraEventBatch): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCameraEventBatchExternalRefLengthRule(var maxLength: Int) : BatchedCameraEventBatchRule {
    override fun check(item: BatchedCameraEventBatch): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCameraEventBatchCodeLengthRule(var maxLength: Int) : BatchedCameraEventBatchRule {
    override fun check(item: BatchedCameraEventBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCameraEventBatchRevisionRangeRule(var min: Int, var max: Int) : BatchedCameraEventBatchRule {
    override fun check(item: BatchedCameraEventBatch): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched camera event batch. */
data class BatchedCameraEventBatchValidator(var rules: MutableList<BatchedCameraEventBatchRule>) {
    fun validate(item: BatchedCameraEventBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCameraEventBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCameraEventBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCameraEventBatchValidator(): BatchedCameraEventBatchValidator {
    return BatchedCameraEventBatchValidator(
        mutableListOf<BatchedCameraEventBatchRule>(
            BatchedCameraEventBatchNameLengthRule(64),
            BatchedCameraEventBatchThresholdRangeRule(0.8, 3584.2),
            BatchedCameraEventBatchExternalRefLengthRule(120),
            BatchedCameraEventBatchCodeLengthRule(255),
            BatchedCameraEventBatchRevisionRangeRule(32, 120),
        ),
    )
}
