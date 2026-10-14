package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.text.normalize

/** A rule a scheduled fuel burn threshold must pass. Check returns its problems, or none. */
interface ScheduledFuelBurnThresholdRule {
    fun check(item: ScheduledFuelBurnThreshold): MutableList<String>
}

data class ScheduledFuelBurnThresholdRequiredReviewerRule(
    var message: String,
) : ScheduledFuelBurnThresholdRule {
    override fun check(item: ScheduledFuelBurnThreshold): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnThresholdActiveGuardRule(var limit: Int) : ScheduledFuelBurnThresholdRule {
    override fun check(item: ScheduledFuelBurnThreshold): MutableList<String> {
        if (item.active && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnThresholdCodeLengthRule(var maxLength: Int) : ScheduledFuelBurnThresholdRule {
    override fun check(item: ScheduledFuelBurnThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnThresholdCodePrefixRule(var prefix: String) : ScheduledFuelBurnThresholdRule {
    override fun check(item: ScheduledFuelBurnThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnThresholdRequiredCodeRule(var message: String) : ScheduledFuelBurnThresholdRule {
    override fun check(item: ScheduledFuelBurnThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledFuelBurnThresholdAllowedReviewerRule(
    var allowed: MutableList<String>,
) : ScheduledFuelBurnThresholdRule {
    override fun check(item: ScheduledFuelBurnThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled fuel burn threshold. */
data class ScheduledFuelBurnThresholdValidator(var rules: MutableList<ScheduledFuelBurnThresholdRule>) {
    fun validate(item: ScheduledFuelBurnThreshold): MutableList<String> {
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

    fun requireValid(item: ScheduledFuelBurnThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledFuelBurnThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledFuelBurnThresholdValidator(): ScheduledFuelBurnThresholdValidator {
    return ScheduledFuelBurnThresholdValidator(
        mutableListOf<ScheduledFuelBurnThresholdRule>(
            ScheduledFuelBurnThresholdRequiredReviewerRule("scheduled fuel burn threshold needs an entry"),
            ScheduledFuelBurnThresholdActiveGuardRule(64),
            ScheduledFuelBurnThresholdCodeLengthRule(80),
            ScheduledFuelBurnThresholdCodePrefixRule("X"),
            ScheduledFuelBurnThresholdRequiredCodeRule("scheduled fuel burn threshold needs this field"),
            ScheduledFuelBurnThresholdAllowedReviewerRule(mutableListOf<String>()),
        ),
    )
}
