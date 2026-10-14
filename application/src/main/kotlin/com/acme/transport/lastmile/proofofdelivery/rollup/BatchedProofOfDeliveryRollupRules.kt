package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.text.normalize

/** A rule a batched proof of delivery rollup must pass. Check returns its problems, or none. */
interface BatchedProofOfDeliveryRollupRule {
    fun check(item: BatchedProofOfDeliveryRollup): MutableList<String>
}

data class BatchedProofOfDeliveryRollupAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedProofOfDeliveryRollupRule {
    override fun check(item: BatchedProofOfDeliveryRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryRollupTitleLengthRule(var maxLength: Int) : BatchedProofOfDeliveryRollupRule {
    override fun check(item: BatchedProofOfDeliveryRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryRollupRequiredCodeRule(
    var message: String,
) : BatchedProofOfDeliveryRollupRule {
    override fun check(item: BatchedProofOfDeliveryRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryRollupCodePrefixRule(var prefix: String) : BatchedProofOfDeliveryRollupRule {
    override fun check(item: BatchedProofOfDeliveryRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedProofOfDeliveryRollupDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : BatchedProofOfDeliveryRollupRule {
    override fun check(item: BatchedProofOfDeliveryRollup): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class BatchedProofOfDeliveryRollupSequenceRangeRule(
    var min: Int,
    var max: Int,
) : BatchedProofOfDeliveryRollupRule {
    override fun check(item: BatchedProofOfDeliveryRollup): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched proof of delivery rollup. */
data class BatchedProofOfDeliveryRollupValidator(var rules: MutableList<BatchedProofOfDeliveryRollupRule>) {
    fun validate(item: BatchedProofOfDeliveryRollup): MutableList<String> {
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

    fun requireValid(item: BatchedProofOfDeliveryRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedProofOfDeliveryRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedProofOfDeliveryRollupValidator(): BatchedProofOfDeliveryRollupValidator {
    return BatchedProofOfDeliveryRollupValidator(
        mutableListOf<BatchedProofOfDeliveryRollupRule>(
            BatchedProofOfDeliveryRollupAllowedCodeRule(mutableListOf<String>("batched")),
            BatchedProofOfDeliveryRollupTitleLengthRule(80),
            BatchedProofOfDeliveryRollupRequiredCodeRule("batched proof of delivery rollup needs this field"),
            BatchedProofOfDeliveryRollupCodePrefixRule("X"),
            BatchedProofOfDeliveryRollupDurationMinutesRangeRule(32, 255),
            BatchedProofOfDeliveryRollupSequenceRangeRule(255, 1000),
        ),
    )
}
