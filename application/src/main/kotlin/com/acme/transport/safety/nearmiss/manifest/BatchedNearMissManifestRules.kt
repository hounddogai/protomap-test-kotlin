package com.acme.transport.safety.nearmiss.manifest

/** A rule a batched near miss manifest must pass. Check returns its problems, or none. */
interface BatchedNearMissManifestRule {
    fun check(item: BatchedNearMissManifest): MutableList<String>
}

data class BatchedNearMissManifestRequiredDescriptionRule(var message: String) : BatchedNearMissManifestRule {
    override fun check(item: BatchedNearMissManifest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissManifestRequiredOwnerIdRule(var message: String) : BatchedNearMissManifestRule {
    override fun check(item: BatchedNearMissManifest): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissManifestAttemptsRangeRule(var min: Int, var max: Int) : BatchedNearMissManifestRule {
    override fun check(item: BatchedNearMissManifest): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissManifestRequiredCodeRule(var message: String) : BatchedNearMissManifestRule {
    override fun check(item: BatchedNearMissManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNearMissManifestTitleLengthRule(var maxLength: Int) : BatchedNearMissManifestRule {
    override fun check(item: BatchedNearMissManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched near miss manifest. */
data class BatchedNearMissManifestValidator(var rules: MutableList<BatchedNearMissManifestRule>) {
    fun validate(item: BatchedNearMissManifest): MutableList<String> {
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

    fun requireValid(item: BatchedNearMissManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNearMissManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNearMissManifestValidator(): BatchedNearMissManifestValidator {
    return BatchedNearMissManifestValidator(
        mutableListOf<BatchedNearMissManifestRule>(
            BatchedNearMissManifestRequiredDescriptionRule("batched near miss manifest needs an entry"),
            BatchedNearMissManifestRequiredOwnerIdRule("batched near miss manifest needs this field"),
            BatchedNearMissManifestAttemptsRangeRule(32, 86400),
            BatchedNearMissManifestRequiredCodeRule("batched near miss manifest needs this field"),
            BatchedNearMissManifestTitleLengthRule(3600),
        ),
    )
}
