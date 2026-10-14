package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize

/** A rule a batched audit window must pass. Check returns its problems, or none. */
interface BatchedAuditWindowRule {
    fun check(item: BatchedAuditWindow): MutableList<String>
}

data class BatchedAuditWindowRequiredTitleRule(var message: String) : BatchedAuditWindowRule {
    override fun check(item: BatchedAuditWindow): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditWindowRequiredCodeRule(var message: String) : BatchedAuditWindowRule {
    override fun check(item: BatchedAuditWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditWindowCodePrefixRule(var prefix: String) : BatchedAuditWindowRule {
    override fun check(item: BatchedAuditWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditWindowRequiredChecksumRule(var message: String) : BatchedAuditWindowRule {
    override fun check(item: BatchedAuditWindow): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditWindowAllowedDriverIdRule(var allowed: MutableList<String>) : BatchedAuditWindowRule {
    override fun check(item: BatchedAuditWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedAuditWindowTitleLengthRule(var maxLength: Int) : BatchedAuditWindowRule {
    override fun check(item: BatchedAuditWindow): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched audit window. */
data class BatchedAuditWindowValidator(var rules: MutableList<BatchedAuditWindowRule>) {
    fun validate(item: BatchedAuditWindow): MutableList<String> {
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

    fun requireValid(item: BatchedAuditWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedAuditWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedAuditWindowValidator(): BatchedAuditWindowValidator {
    return BatchedAuditWindowValidator(
        mutableListOf<BatchedAuditWindowRule>(
            BatchedAuditWindowRequiredTitleRule("batched audit window needs a value"),
            BatchedAuditWindowRequiredCodeRule("batched audit window needs this field"),
            BatchedAuditWindowCodePrefixRule("OPS"),
            BatchedAuditWindowRequiredChecksumRule("batched audit window needs an entry"),
            BatchedAuditWindowAllowedDriverIdRule(mutableListOf<String>()),
            BatchedAuditWindowTitleLengthRule(86400),
        ),
    )
}
