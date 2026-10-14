package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.text.normalize

/** A rule a batched drop digest must pass. Check returns its problems, or none. */
interface BatchedDropDigestRule {
    fun check(item: BatchedDropDigest): MutableList<String>
}

data class BatchedDropDigestTitleLengthRule(var maxLength: Int) : BatchedDropDigestRule {
    override fun check(item: BatchedDropDigest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropDigestOwnerIdLengthRule(var maxLength: Int) : BatchedDropDigestRule {
    override fun check(item: BatchedDropDigest): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropDigestCodePrefixRule(var prefix: String) : BatchedDropDigestRule {
    override fun check(item: BatchedDropDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropDigestRequiredCodeRule(var message: String) : BatchedDropDigestRule {
    override fun check(item: BatchedDropDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropDigestAllowedTitleRule(var allowed: MutableList<String>) : BatchedDropDigestRule {
    override fun check(item: BatchedDropDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDropDigestRequiredExternalRefRule(var message: String) : BatchedDropDigestRule {
    override fun check(item: BatchedDropDigest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched drop digest. */
data class BatchedDropDigestValidator(var rules: MutableList<BatchedDropDigestRule>) {
    fun validate(item: BatchedDropDigest): MutableList<String> {
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

    fun requireValid(item: BatchedDropDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDropDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDropDigestValidator(): BatchedDropDigestValidator {
    return BatchedDropDigestValidator(
        mutableListOf<BatchedDropDigestRule>(
            BatchedDropDigestTitleLengthRule(32),
            BatchedDropDigestOwnerIdLengthRule(1000),
            BatchedDropDigestCodePrefixRule("OPS"),
            BatchedDropDigestRequiredCodeRule("batched drop digest needs an entry"),
            BatchedDropDigestAllowedTitleRule(mutableListOf<String>("manual", "default", "regional")),
            BatchedDropDigestRequiredExternalRefRule("batched drop digest needs a value"),
        ),
    )
}
