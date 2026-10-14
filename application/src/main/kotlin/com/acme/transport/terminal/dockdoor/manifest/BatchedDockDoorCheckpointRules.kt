package com.acme.transport.terminal.dockdoor.manifest

/** A rule a batched dock door checkpoint must pass. Check returns its problems, or none. */
interface BatchedDockDoorCheckpointRule {
    fun check(item: BatchedDockDoorCheckpoint): MutableList<String>
}

data class BatchedDockDoorCheckpointLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : BatchedDockDoorCheckpointRule {
    override fun check(item: BatchedDockDoorCheckpoint): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorCheckpointTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedDockDoorCheckpointRule {
    override fun check(item: BatchedDockDoorCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class BatchedDockDoorCheckpointRequiredCodeRule(var message: String) : BatchedDockDoorCheckpointRule {
    override fun check(item: BatchedDockDoorCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorCheckpointCodePrefixRule(var prefix: String) : BatchedDockDoorCheckpointRule {
    override fun check(item: BatchedDockDoorCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDockDoorCheckpointTitleLengthRule(var maxLength: Int) : BatchedDockDoorCheckpointRule {
    override fun check(item: BatchedDockDoorCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched dock door checkpoint. */
data class BatchedDockDoorCheckpointValidator(var rules: MutableList<BatchedDockDoorCheckpointRule>) {
    fun validate(item: BatchedDockDoorCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedDockDoorCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDockDoorCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDockDoorCheckpointValidator(): BatchedDockDoorCheckpointValidator {
    return BatchedDockDoorCheckpointValidator(
        mutableListOf<BatchedDockDoorCheckpointRule>(
            BatchedDockDoorCheckpointLoadKgRangeRule(2.3, 4430.1),
            BatchedDockDoorCheckpointTagsLimitRule(16, mutableListOf<String>("seasonal", "lenient")),
            BatchedDockDoorCheckpointRequiredCodeRule("batched dock door checkpoint needs this field"),
            BatchedDockDoorCheckpointCodePrefixRule("Q"),
            BatchedDockDoorCheckpointTitleLengthRule(16),
        ),
    )
}
