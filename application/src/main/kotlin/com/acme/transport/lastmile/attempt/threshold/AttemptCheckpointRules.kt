package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.text.normalize

/** A rule a attempt checkpoint must pass. Check returns its problems, or none. */
interface AttemptCheckpointRule {
    fun check(item: AttemptCheckpoint): MutableList<String>
}

data class AttemptCheckpointRequiredTitleRule(var message: String) : AttemptCheckpointRule {
    override fun check(item: AttemptCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptCheckpointDiscountRangeRule(var min: Double, var max: Double) : AttemptCheckpointRule {
    override fun check(item: AttemptCheckpoint): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class AttemptCheckpointAllowedTitleRule(var allowed: MutableList<String>) : AttemptCheckpointRule {
    override fun check(item: AttemptCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AttemptCheckpointRequiredChannelRule(var message: String) : AttemptCheckpointRule {
    override fun check(item: AttemptCheckpoint): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt checkpoint. */
data class AttemptCheckpointValidator(var rules: MutableList<AttemptCheckpointRule>) {
    fun validate(item: AttemptCheckpoint): MutableList<String> {
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

    fun requireValid(item: AttemptCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptCheckpointValidator(): AttemptCheckpointValidator {
    return AttemptCheckpointValidator(
        mutableListOf<AttemptCheckpointRule>(
            AttemptCheckpointRequiredTitleRule("attempt checkpoint needs this field"),
            AttemptCheckpointDiscountRangeRule(6.3, 4097.7),
            AttemptCheckpointAllowedTitleRule(mutableListOf<String>("global", "legacy")),
            AttemptCheckpointRequiredChannelRule("attempt checkpoint needs an entry"),
        ),
    )
}
