package com.acme.transport.network.node.slot

import com.acme.transport.kit.text.normalize

/** A rule a batched node notice must pass. Check returns its problems, or none. */
interface BatchedNodeNoticeRule {
    fun check(item: BatchedNodeNotice): MutableList<String>
}

data class BatchedNodeNoticeOdometerRangeRule(var min: Int, var max: Int) : BatchedNodeNoticeRule {
    override fun check(item: BatchedNodeNotice): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class BatchedNodeNoticeCodePrefixRule(var prefix: String) : BatchedNodeNoticeRule {
    override fun check(item: BatchedNodeNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeNoticeRequiredNameRule(var message: String) : BatchedNodeNoticeRule {
    override fun check(item: BatchedNodeNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeNoticeNameLengthRule(var maxLength: Int) : BatchedNodeNoticeRule {
    override fun check(item: BatchedNodeNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeNoticeChannelLengthRule(var maxLength: Int) : BatchedNodeNoticeRule {
    override fun check(item: BatchedNodeNotice): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeNoticeAllowedCodeRule(var allowed: MutableList<String>) : BatchedNodeNoticeRule {
    override fun check(item: BatchedNodeNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched node notice. */
data class BatchedNodeNoticeValidator(var rules: MutableList<BatchedNodeNoticeRule>) {
    fun validate(item: BatchedNodeNotice): MutableList<String> {
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

    fun requireValid(item: BatchedNodeNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodeNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodeNoticeValidator(): BatchedNodeNoticeValidator {
    return BatchedNodeNoticeValidator(
        mutableListOf<BatchedNodeNoticeRule>(
            BatchedNodeNoticeOdometerRangeRule(32, 86400),
            BatchedNodeNoticeCodePrefixRule("REF"),
            BatchedNodeNoticeRequiredNameRule("batched node notice needs this field"),
            BatchedNodeNoticeNameLengthRule(80),
            BatchedNodeNoticeChannelLengthRule(64),
            BatchedNodeNoticeAllowedCodeRule(mutableListOf<String>("weighted", "primary", "lenient")),
        ),
    )
}
