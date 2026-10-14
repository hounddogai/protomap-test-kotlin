package com.acme.transport.safety.cameraevent.globaleventaudit

/** A rule a batched camera event allocation must pass. Check returns its problems, or none. */
interface BatchedCameraEventAllocationRule {
    fun check(item: BatchedCameraEventAllocation): MutableList<String>
}

data class BatchedCameraEventAllocationTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedCameraEventAllocationRule {
    override fun check(item: BatchedCameraEventAllocation): MutableList<String> {
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

data class BatchedCameraEventAllocationRequiredOwnerIdRule(
    var message: String,
) : BatchedCameraEventAllocationRule {
    override fun check(item: BatchedCameraEventAllocation): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedCameraEventAllocationWeightRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCameraEventAllocationRule {
    override fun check(item: BatchedCameraEventAllocation): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched camera event allocation. */
data class BatchedCameraEventAllocationValidator(var rules: MutableList<BatchedCameraEventAllocationRule>) {
    fun validate(item: BatchedCameraEventAllocation): MutableList<String> {
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

    fun requireValid(item: BatchedCameraEventAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCameraEventAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCameraEventAllocationValidator(): BatchedCameraEventAllocationValidator {
    return BatchedCameraEventAllocationValidator(
        mutableListOf<BatchedCameraEventAllocationRule>(
            BatchedCameraEventAllocationTagsLimitRule(
                64,
                mutableListOf<String>("batched", "seasonal", "weighted"),
            ),
            BatchedCameraEventAllocationRequiredOwnerIdRule("batched camera event allocation needs a value"),
            BatchedCameraEventAllocationWeightRangeRule(1.9, 787.7),
        ),
    )
}
