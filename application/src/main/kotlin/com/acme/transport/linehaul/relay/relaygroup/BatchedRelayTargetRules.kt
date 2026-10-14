package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.text.normalize

/** A rule a batched relay target must pass. Check returns its problems, or none. */
interface BatchedRelayTargetRule {
    fun check(item: BatchedRelayTarget): MutableList<String>
}

data class BatchedRelayTargetAllowedDriverIdRule(var allowed: MutableList<String>) : BatchedRelayTargetRule {
    override fun check(item: BatchedRelayTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayTargetRequiredTitleRule(var message: String) : BatchedRelayTargetRule {
    override fun check(item: BatchedRelayTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayTargetPalletsRangeRule(var min: Int, var max: Int) : BatchedRelayTargetRule {
    override fun check(item: BatchedRelayTarget): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class BatchedRelayTargetTitleLengthRule(var maxLength: Int) : BatchedRelayTargetRule {
    override fun check(item: BatchedRelayTarget): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRelayTargetReviewerLengthRule(var maxLength: Int) : BatchedRelayTargetRule {
    override fun check(item: BatchedRelayTarget): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched relay target. */
data class BatchedRelayTargetValidator(var rules: MutableList<BatchedRelayTargetRule>) {
    fun validate(item: BatchedRelayTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedRelayTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRelayTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRelayTargetValidator(): BatchedRelayTargetValidator {
    return BatchedRelayTargetValidator(
        mutableListOf<BatchedRelayTargetRule>(
            BatchedRelayTargetAllowedDriverIdRule(mutableListOf<String>("manual", "seasonal")),
            BatchedRelayTargetRequiredTitleRule("batched relay target needs a value"),
            BatchedRelayTargetPalletsRangeRule(16, 86400),
            BatchedRelayTargetTitleLengthRule(86400),
            BatchedRelayTargetReviewerLengthRule(86400),
        ),
    )
}
