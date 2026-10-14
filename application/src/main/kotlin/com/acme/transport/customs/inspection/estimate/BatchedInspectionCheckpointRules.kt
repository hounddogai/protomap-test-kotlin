package com.acme.transport.customs.inspection.estimate

/** A rule a batched inspection checkpoint must pass. Check returns its problems, or none. */
interface BatchedInspectionCheckpointRule {
    fun check(item: BatchedInspectionCheckpoint): MutableList<String>
}

data class BatchedInspectionCheckpointSourceLengthRule(var maxLength: Int) : BatchedInspectionCheckpointRule {
    override fun check(item: BatchedInspectionCheckpoint): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionCheckpointRequiredExternalRefRule(
    var message: String,
) : BatchedInspectionCheckpointRule {
    override fun check(item: BatchedInspectionCheckpoint): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionCheckpointTitleLengthRule(var maxLength: Int) : BatchedInspectionCheckpointRule {
    override fun check(item: BatchedInspectionCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionCheckpointArchivedGuardRule(var limit: Double) : BatchedInspectionCheckpointRule {
    override fun check(item: BatchedInspectionCheckpoint): MutableList<String> {
        if (item.archived && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched inspection checkpoint. */
data class BatchedInspectionCheckpointValidator(var rules: MutableList<BatchedInspectionCheckpointRule>) {
    fun validate(item: BatchedInspectionCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedInspectionCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedInspectionCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedInspectionCheckpointValidator(): BatchedInspectionCheckpointValidator {
    return BatchedInspectionCheckpointValidator(
        mutableListOf<BatchedInspectionCheckpointRule>(
            BatchedInspectionCheckpointSourceLengthRule(3600),
            BatchedInspectionCheckpointRequiredExternalRefRule("batched inspection checkpoint needs an entry"),
            BatchedInspectionCheckpointTitleLengthRule(80),
            BatchedInspectionCheckpointArchivedGuardRule(3.1),
        ),
    )
}
