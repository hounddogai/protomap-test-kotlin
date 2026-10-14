package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.text.normalize

/** A rule a pending transit time checkpoint must pass. Check returns its problems, or none. */
interface PendingTransitTimeCheckpointRule {
    fun check(item: PendingTransitTimeCheckpoint): MutableList<String>
}

data class PendingTransitTimeCheckpointTitleLengthRule(var maxLength: Int) : PendingTransitTimeCheckpointRule {
    override fun check(item: PendingTransitTimeCheckpoint): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeCheckpointAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : PendingTransitTimeCheckpointRule {
    override fun check(item: PendingTransitTimeCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeCheckpointLimitRangeRule(
    var min: Int,
    var max: Int,
) : PendingTransitTimeCheckpointRule {
    override fun check(item: PendingTransitTimeCheckpoint): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeCheckpointUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : PendingTransitTimeCheckpointRule {
    override fun check(item: PendingTransitTimeCheckpoint): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class PendingTransitTimeCheckpointCodePrefixRule(var prefix: String) : PendingTransitTimeCheckpointRule {
    override fun check(item: PendingTransitTimeCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeCheckpointRequiredCodeRule(
    var message: String,
) : PendingTransitTimeCheckpointRule {
    override fun check(item: PendingTransitTimeCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending transit time checkpoint. */
data class PendingTransitTimeCheckpointValidator(var rules: MutableList<PendingTransitTimeCheckpointRule>) {
    fun validate(item: PendingTransitTimeCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTransitTimeCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTransitTimeCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTransitTimeCheckpointValidator(): PendingTransitTimeCheckpointValidator {
    return PendingTransitTimeCheckpointValidator(
        mutableListOf<PendingTransitTimeCheckpointRule>(
            PendingTransitTimeCheckpointTitleLengthRule(32),
            PendingTransitTimeCheckpointAllowedTimezoneRule(mutableListOf<String>("manual")),
            PendingTransitTimeCheckpointLimitRangeRule(16, 80),
            PendingTransitTimeCheckpointUpdatedAtRangeRule(16, 255),
            PendingTransitTimeCheckpointCodePrefixRule("Q"),
            PendingTransitTimeCheckpointRequiredCodeRule("pending transit time checkpoint needs this field"),
        ),
    )
}
