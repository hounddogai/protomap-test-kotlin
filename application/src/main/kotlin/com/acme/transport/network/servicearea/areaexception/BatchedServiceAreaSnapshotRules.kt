package com.acme.transport.network.servicearea.areaexception

/** A rule a batched service area snapshot must pass. Check returns its problems, or none. */
interface BatchedServiceAreaSnapshotRule {
    fun check(item: BatchedServiceAreaSnapshot): MutableList<String>
}

data class BatchedServiceAreaSnapshotNameLengthRule(var maxLength: Int) : BatchedServiceAreaSnapshotRule {
    override fun check(item: BatchedServiceAreaSnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaSnapshotAmountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedServiceAreaSnapshotRule {
    override fun check(item: BatchedServiceAreaSnapshot): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaSnapshotWeightRangeRule(
    var min: Double,
    var max: Double,
) : BatchedServiceAreaSnapshotRule {
    override fun check(item: BatchedServiceAreaSnapshot): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedServiceAreaSnapshotRequiredCodeRule(var message: String) : BatchedServiceAreaSnapshotRule {
    override fun check(item: BatchedServiceAreaSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched service area snapshot. */
data class BatchedServiceAreaSnapshotValidator(var rules: MutableList<BatchedServiceAreaSnapshotRule>) {
    fun validate(item: BatchedServiceAreaSnapshot): MutableList<String> {
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

    fun requireValid(item: BatchedServiceAreaSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedServiceAreaSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedServiceAreaSnapshotValidator(): BatchedServiceAreaSnapshotValidator {
    return BatchedServiceAreaSnapshotValidator(
        mutableListOf<BatchedServiceAreaSnapshotRule>(
            BatchedServiceAreaSnapshotNameLengthRule(120),
            BatchedServiceAreaSnapshotAmountRangeRule(2.6, 1417.4),
            BatchedServiceAreaSnapshotWeightRangeRule(3.9, 1858.2),
            BatchedServiceAreaSnapshotRequiredCodeRule("batched service area snapshot needs a value"),
        ),
    )
}
