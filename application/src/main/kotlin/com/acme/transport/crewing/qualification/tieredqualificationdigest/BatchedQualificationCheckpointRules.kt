package com.acme.transport.crewing.qualification.tieredqualificationdigest

/** A rule a batched qualification checkpoint must pass. Check returns its problems, or none. */
interface BatchedQualificationCheckpointRule {
    fun check(item: BatchedQualificationCheckpoint): MutableList<String>
}

data class BatchedQualificationCheckpointCodePrefixRule(
    var prefix: String,
) : BatchedQualificationCheckpointRule {
    override fun check(item: BatchedQualificationCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCheckpointUnitLengthRule(
    var maxLength: Int,
) : BatchedQualificationCheckpointRule {
    override fun check(item: BatchedQualificationCheckpoint): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCheckpointRequiredUnitRule(
    var message: String,
) : BatchedQualificationCheckpointRule {
    override fun check(item: BatchedQualificationCheckpoint): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCheckpointScoreRangeRule(
    var min: Double,
    var max: Double,
) : BatchedQualificationCheckpointRule {
    override fun check(item: BatchedQualificationCheckpoint): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCheckpointRequiredCodeRule(
    var message: String,
) : BatchedQualificationCheckpointRule {
    override fun check(item: BatchedQualificationCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedQualificationCheckpointTitleLengthRule(
    var maxLength: Int,
) : BatchedQualificationCheckpointRule {
    override fun check(item: BatchedQualificationCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched qualification checkpoint. */
data class BatchedQualificationCheckpointValidator(var rules: MutableList<BatchedQualificationCheckpointRule>) {
    fun validate(item: BatchedQualificationCheckpoint): MutableList<String> {
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

    fun requireValid(item: BatchedQualificationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedQualificationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedQualificationCheckpointValidator(): BatchedQualificationCheckpointValidator {
    return BatchedQualificationCheckpointValidator(
        mutableListOf<BatchedQualificationCheckpointRule>(
            BatchedQualificationCheckpointCodePrefixRule("Q"),
            BatchedQualificationCheckpointUnitLengthRule(32),
            BatchedQualificationCheckpointRequiredUnitRule("batched qualification checkpoint needs a value"),
            BatchedQualificationCheckpointScoreRangeRule(8.3, 3598.3),
            BatchedQualificationCheckpointRequiredCodeRule("batched qualification checkpoint needs an entry"),
            BatchedQualificationCheckpointTitleLengthRule(32),
        ),
    )
}
