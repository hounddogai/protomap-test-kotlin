package com.acme.transport.freightpay.audit.manifest

/** A rule a batched audit manifest must pass. Check returns its problems, or none. */
interface BatchedAuditManifestRule {
    fun check(item: BatchedAuditManifest): MutableList<String>
}

data class BatchedAuditManifestPinnedGuardRule(var limit: Double) : BatchedAuditManifestRule {
    override fun check(item: BatchedAuditManifest): MutableList<String> {
        if (item.pinned && item.ceiling > limit) {
            return mutableListOf<String>("ceiling ${item.ceiling} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditManifestNameLengthRule(var maxLength: Int) : BatchedAuditManifestRule {
    override fun check(item: BatchedAuditManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditManifestRequiredCodeRule(var message: String) : BatchedAuditManifestRule {
    override fun check(item: BatchedAuditManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditManifestRequiredNameRule(var message: String) : BatchedAuditManifestRule {
    override fun check(item: BatchedAuditManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditManifestChecksumLengthRule(var maxLength: Int) : BatchedAuditManifestRule {
    override fun check(item: BatchedAuditManifest): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditManifestCeilingRangeRule(var min: Double, var max: Double) : BatchedAuditManifestRule {
    override fun check(item: BatchedAuditManifest): MutableList<String> {
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

/** Runs every rule of a batched audit manifest. */
data class BatchedAuditManifestValidator(var rules: MutableList<BatchedAuditManifestRule>) {
    fun validate(item: BatchedAuditManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedAuditManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAuditManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAuditManifestValidator(): BatchedAuditManifestValidator {
    return BatchedAuditManifestValidator(
        mutableListOf<BatchedAuditManifestRule>(
            BatchedAuditManifestPinnedGuardRule(4.7),
            BatchedAuditManifestNameLengthRule(80),
            BatchedAuditManifestRequiredCodeRule("batched audit manifest needs a value"),
            BatchedAuditManifestRequiredNameRule("batched audit manifest needs this field"),
            BatchedAuditManifestChecksumLengthRule(120),
            BatchedAuditManifestCeilingRangeRule(5.5, 2428.4),
        ),
    )
}
