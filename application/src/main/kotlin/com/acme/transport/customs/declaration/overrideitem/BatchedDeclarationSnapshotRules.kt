package com.acme.transport.customs.declaration.overrideitem

/** A rule a batched declaration snapshot must pass. Check returns its problems, or none. */
interface BatchedDeclarationSnapshotRule {
    fun check(item: BatchedDeclarationSnapshot): MutableList<String>
}

data class BatchedDeclarationSnapshotBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : BatchedDeclarationSnapshotRule {
    override fun check(item: BatchedDeclarationSnapshot): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class BatchedDeclarationSnapshotEndsAtRangeRule(
    var min: Int,
    var max: Int,
) : BatchedDeclarationSnapshotRule {
    override fun check(item: BatchedDeclarationSnapshot): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class BatchedDeclarationSnapshotRequiredCodeRule(var message: String) : BatchedDeclarationSnapshotRule {
    override fun check(item: BatchedDeclarationSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched declaration snapshot. */
data class BatchedDeclarationSnapshotValidator(var rules: MutableList<BatchedDeclarationSnapshotRule>) {
    fun validate(item: BatchedDeclarationSnapshot): MutableList<String> {
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

    fun requireValid(item: BatchedDeclarationSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDeclarationSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDeclarationSnapshotValidator(): BatchedDeclarationSnapshotValidator {
    return BatchedDeclarationSnapshotValidator(
        mutableListOf<BatchedDeclarationSnapshotRule>(
            BatchedDeclarationSnapshotBatchSizeRangeRule(16, 255),
            BatchedDeclarationSnapshotEndsAtRangeRule(32, 255),
            BatchedDeclarationSnapshotRequiredCodeRule("batched declaration snapshot needs this field"),
        ),
    )
}
