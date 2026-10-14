package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.normalize

/** A rule a batched trailer yard policy must pass. Check returns its problems, or none. */
interface BatchedTrailerYardPolicyRule {
    fun check(item: BatchedTrailerYardPolicy): MutableList<String>
}

data class BatchedTrailerYardPolicyEndsAtRangeRule(var min: Int, var max: Int) : BatchedTrailerYardPolicyRule {
    override fun check(item: BatchedTrailerYardPolicy): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class BatchedTrailerYardPolicyCodePrefixRule(var prefix: String) : BatchedTrailerYardPolicyRule {
    override fun check(item: BatchedTrailerYardPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerYardPolicyAllowedLabelRule(
    var allowed: MutableList<String>,
) : BatchedTrailerYardPolicyRule {
    override fun check(item: BatchedTrailerYardPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer yard policy. */
data class BatchedTrailerYardPolicyValidator(var rules: MutableList<BatchedTrailerYardPolicyRule>) {
    fun validate(item: BatchedTrailerYardPolicy): MutableList<String> {
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

    fun requireValid(item: BatchedTrailerYardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerYardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerYardPolicyValidator(): BatchedTrailerYardPolicyValidator {
    return BatchedTrailerYardPolicyValidator(
        mutableListOf<BatchedTrailerYardPolicyRule>(
            BatchedTrailerYardPolicyEndsAtRangeRule(255, 3600),
            BatchedTrailerYardPolicyCodePrefixRule("OPS"),
            BatchedTrailerYardPolicyAllowedLabelRule(mutableListOf<String>("strict")),
        ),
    )
}
