package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.text.normalize

/** A rule a batched sortation override must pass. Check returns its problems, or none. */
interface BatchedSortationOverrideRule {
    fun check(item: BatchedSortationOverride): MutableList<String>
}

data class BatchedSortationOverrideRequiredLaneRule(var message: String) : BatchedSortationOverrideRule {
    override fun check(item: BatchedSortationOverride): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedSortationOverrideAllowedPhoneRule(
    var allowed: MutableList<String>,
) : BatchedSortationOverrideRule {
    override fun check(item: BatchedSortationOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSortationOverrideLabelLengthRule(var maxLength: Int) : BatchedSortationOverrideRule {
    override fun check(item: BatchedSortationOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedSortationOverrideTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedSortationOverrideRule {
    override fun check(item: BatchedSortationOverride): MutableList<String> {
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

/** Runs every rule of a batched sortation override. */
data class BatchedSortationOverrideValidator(var rules: MutableList<BatchedSortationOverrideRule>) {
    fun validate(item: BatchedSortationOverride): MutableList<String> {
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

    fun requireValid(item: BatchedSortationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedSortationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedSortationOverrideValidator(): BatchedSortationOverrideValidator {
    return BatchedSortationOverrideValidator(
        mutableListOf<BatchedSortationOverrideRule>(
            BatchedSortationOverrideRequiredLaneRule("batched sortation override needs an entry"),
            BatchedSortationOverrideAllowedPhoneRule(mutableListOf<String>("adaptive")),
            BatchedSortationOverrideLabelLengthRule(255),
            BatchedSortationOverrideTagsLimitRule(32, mutableListOf<String>("capped")),
        ),
    )
}
