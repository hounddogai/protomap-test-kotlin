package com.acme.transport.customs.broker.agreement

import com.acme.transport.kit.text.normalize

/** A rule a fallback broker checkpoint must pass. Check returns its problems, or none. */
interface FallbackBrokerCheckpointRule {
    fun check(item: FallbackBrokerCheckpoint): MutableList<String>
}

data class FallbackBrokerCheckpointLabelLengthRule(var maxLength: Int) : FallbackBrokerCheckpointRule {
    override fun check(item: FallbackBrokerCheckpoint): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBrokerCheckpointRequiredOwnerIdRule(var message: String) : FallbackBrokerCheckpointRule {
    override fun check(item: FallbackBrokerCheckpoint): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBrokerCheckpointAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : FallbackBrokerCheckpointRule {
    override fun check(item: FallbackBrokerCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBrokerCheckpointRequiredReviewerRule(var message: String) : FallbackBrokerCheckpointRule {
    override fun check(item: FallbackBrokerCheckpoint): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBrokerCheckpointGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackBrokerCheckpointRule {
    override fun check(item: FallbackBrokerCheckpoint): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback broker checkpoint. */
data class FallbackBrokerCheckpointValidator(var rules: MutableList<FallbackBrokerCheckpointRule>) {
    fun validate(item: FallbackBrokerCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackBrokerCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBrokerCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBrokerCheckpointValidator(): FallbackBrokerCheckpointValidator {
    return FallbackBrokerCheckpointValidator(
        mutableListOf<FallbackBrokerCheckpointRule>(
            FallbackBrokerCheckpointLabelLengthRule(255),
            FallbackBrokerCheckpointRequiredOwnerIdRule("fallback broker checkpoint needs a value"),
            FallbackBrokerCheckpointAllowedOwnerIdRule(mutableListOf<String>()),
            FallbackBrokerCheckpointRequiredReviewerRule("fallback broker checkpoint needs an entry"),
            FallbackBrokerCheckpointGraceDaysRangeRule(32, 3600),
        ),
    )
}
