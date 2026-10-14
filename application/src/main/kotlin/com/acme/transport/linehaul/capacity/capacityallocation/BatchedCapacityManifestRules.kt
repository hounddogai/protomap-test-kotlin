package com.acme.transport.linehaul.capacity.capacityallocation

/** A rule a batched capacity manifest must pass. Check returns its problems, or none. */
interface BatchedCapacityManifestRule {
    fun check(item: BatchedCapacityManifest): MutableList<String>
}

data class BatchedCapacityManifestNotesLengthRule(var maxLength: Int) : BatchedCapacityManifestRule {
    override fun check(item: BatchedCapacityManifest): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCapacityManifestCeilingRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCapacityManifestRule {
    override fun check(item: BatchedCapacityManifest): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class BatchedCapacityManifestBudgetRangeRule(
    var min: Double,
    var max: Double,
) : BatchedCapacityManifestRule {
    override fun check(item: BatchedCapacityManifest): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class BatchedCapacityManifestRequiredExternalRefRule(var message: String) : BatchedCapacityManifestRule {
    override fun check(item: BatchedCapacityManifest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched capacity manifest. */
data class BatchedCapacityManifestValidator(var rules: MutableList<BatchedCapacityManifestRule>) {
    fun validate(item: BatchedCapacityManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCapacityManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCapacityManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCapacityManifestValidator(): BatchedCapacityManifestValidator {
    return BatchedCapacityManifestValidator(
        mutableListOf<BatchedCapacityManifestRule>(
            BatchedCapacityManifestNotesLengthRule(120),
            BatchedCapacityManifestCeilingRangeRule(2.0, 1140.7),
            BatchedCapacityManifestBudgetRangeRule(6.8, 1100.4),
            BatchedCapacityManifestRequiredExternalRefRule("batched capacity manifest needs a value"),
        ),
    )
}
