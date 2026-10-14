package com.acme.transport.linehaul.backhaul.preference

/** A rule a batched backhaul digest must pass. Check returns its problems, or none. */
interface BatchedBackhaulDigestRule {
    fun check(item: BatchedBackhaulDigest): MutableList<String>
}

data class BatchedBackhaulDigestPinnedGuardRule(var limit: Int) : BatchedBackhaulDigestRule {
    override fun check(item: BatchedBackhaulDigest): MutableList<String> {
        if (item.pinned && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulDigestWeightRangeRule(var min: Double, var max: Double) : BatchedBackhaulDigestRule {
    override fun check(item: BatchedBackhaulDigest): MutableList<String> {
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

data class BatchedBackhaulDigestCodePrefixRule(var prefix: String) : BatchedBackhaulDigestRule {
    override fun check(item: BatchedBackhaulDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulDigestRequiredCodeRule(var message: String) : BatchedBackhaulDigestRule {
    override fun check(item: BatchedBackhaulDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched backhaul digest. */
data class BatchedBackhaulDigestValidator(var rules: MutableList<BatchedBackhaulDigestRule>) {
    fun validate(item: BatchedBackhaulDigest): MutableList<String> {
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

    fun requireValid(item: BatchedBackhaulDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedBackhaulDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedBackhaulDigestValidator(): BatchedBackhaulDigestValidator {
    return BatchedBackhaulDigestValidator(
        mutableListOf<BatchedBackhaulDigestRule>(
            BatchedBackhaulDigestPinnedGuardRule(1000),
            BatchedBackhaulDigestWeightRangeRule(9.0, 3041.6),
            BatchedBackhaulDigestCodePrefixRule("ACM"),
            BatchedBackhaulDigestRequiredCodeRule("batched backhaul digest needs an entry"),
        ),
    )
}
