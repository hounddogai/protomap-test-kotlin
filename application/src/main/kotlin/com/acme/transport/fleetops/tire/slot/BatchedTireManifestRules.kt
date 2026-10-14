package com.acme.transport.fleetops.tire.slot

/** A rule a batched tire manifest must pass. Check returns its problems, or none. */
interface BatchedTireManifestRule {
    fun check(item: BatchedTireManifest): MutableList<String>
}

data class BatchedTireManifestCodeLengthRule(var maxLength: Int) : BatchedTireManifestRule {
    override fun check(item: BatchedTireManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTireManifestRequiredDepotRule(var message: String) : BatchedTireManifestRule {
    override fun check(item: BatchedTireManifest): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTireManifestLabelLengthRule(var maxLength: Int) : BatchedTireManifestRule {
    override fun check(item: BatchedTireManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTireManifestRequiredLabelRule(var message: String) : BatchedTireManifestRule {
    override fun check(item: BatchedTireManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched tire manifest. */
data class BatchedTireManifestValidator(var rules: MutableList<BatchedTireManifestRule>) {
    fun validate(item: BatchedTireManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTireManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTireManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTireManifestValidator(): BatchedTireManifestValidator {
    return BatchedTireManifestValidator(
        mutableListOf<BatchedTireManifestRule>(
            BatchedTireManifestCodeLengthRule(16),
            BatchedTireManifestRequiredDepotRule("batched tire manifest needs this field"),
            BatchedTireManifestLabelLengthRule(32),
            BatchedTireManifestRequiredLabelRule("batched tire manifest needs a value"),
        ),
    )
}
