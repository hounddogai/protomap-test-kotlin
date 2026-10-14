package com.acme.transport.crewing.availability.scheduledavailabilitytier

/** A rule a batched availability tier must pass. Check returns its problems, or none. */
interface BatchedAvailabilityTierRule {
    fun check(item: BatchedAvailabilityTier): MutableList<String>
}

data class BatchedAvailabilityTierNameLengthRule(var maxLength: Int) : BatchedAvailabilityTierRule {
    override fun check(item: BatchedAvailabilityTier): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityTierPinnedGuardRule(var limit: Int) : BatchedAvailabilityTierRule {
    override fun check(item: BatchedAvailabilityTier): MutableList<String> {
        if (item.pinned && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityTierCodePrefixRule(var prefix: String) : BatchedAvailabilityTierRule {
    override fun check(item: BatchedAvailabilityTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAvailabilityTierRequiredNameRule(var message: String) : BatchedAvailabilityTierRule {
    override fun check(item: BatchedAvailabilityTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched availability tier. */
data class BatchedAvailabilityTierValidator(var rules: MutableList<BatchedAvailabilityTierRule>) {
    fun validate(item: BatchedAvailabilityTier): MutableList<String> {
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

    fun requireValid(item: BatchedAvailabilityTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAvailabilityTier>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAvailabilityTierValidator(): BatchedAvailabilityTierValidator {
    return BatchedAvailabilityTierValidator(
        mutableListOf<BatchedAvailabilityTierRule>(
            BatchedAvailabilityTierNameLengthRule(255),
            BatchedAvailabilityTierPinnedGuardRule(255),
            BatchedAvailabilityTierCodePrefixRule("ACM"),
            BatchedAvailabilityTierRequiredNameRule("batched availability tier needs an entry"),
        ),
    )
}
