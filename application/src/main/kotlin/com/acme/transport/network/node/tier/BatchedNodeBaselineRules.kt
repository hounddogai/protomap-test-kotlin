package com.acme.transport.network.node.tier

import com.acme.transport.kit.text.normalize

/** A rule a batched node baseline must pass. Check returns its problems, or none. */
interface BatchedNodeBaselineRule {
    fun check(item: BatchedNodeBaseline): MutableList<String>
}

data class BatchedNodeBaselineRequiredCodeRule(var message: String) : BatchedNodeBaselineRule {
    override fun check(item: BatchedNodeBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeBaselineAllowedTrailerIdRule(var allowed: MutableList<String>) : BatchedNodeBaselineRule {
    override fun check(item: BatchedNodeBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeBaselineCodePrefixRule(var prefix: String) : BatchedNodeBaselineRule {
    override fun check(item: BatchedNodeBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeBaselineTrailerIdLengthRule(var maxLength: Int) : BatchedNodeBaselineRule {
    override fun check(item: BatchedNodeBaseline): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeBaselineContactEmailLengthRule(var maxLength: Int) : BatchedNodeBaselineRule {
    override fun check(item: BatchedNodeBaseline): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeBaselineTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedNodeBaselineRule {
    override fun check(item: BatchedNodeBaseline): MutableList<String> {
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

/** Runs every rule of a batched node baseline. */
data class BatchedNodeBaselineValidator(var rules: MutableList<BatchedNodeBaselineRule>) {
    fun validate(item: BatchedNodeBaseline): MutableList<String> {
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

    fun requireValid(item: BatchedNodeBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodeBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodeBaselineValidator(): BatchedNodeBaselineValidator {
    return BatchedNodeBaselineValidator(
        mutableListOf<BatchedNodeBaselineRule>(
            BatchedNodeBaselineRequiredCodeRule("batched node baseline needs an entry"),
            BatchedNodeBaselineAllowedTrailerIdRule(mutableListOf<String>("seasonal")),
            BatchedNodeBaselineCodePrefixRule("X"),
            BatchedNodeBaselineTrailerIdLengthRule(1000),
            BatchedNodeBaselineContactEmailLengthRule(86400),
            BatchedNodeBaselineTagsLimitRule(64, mutableListOf<String>("regional", "seasonal")),
        ),
    )
}
