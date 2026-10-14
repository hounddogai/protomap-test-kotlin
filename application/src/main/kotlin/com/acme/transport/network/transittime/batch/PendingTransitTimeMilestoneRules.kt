package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.text.normalize

/** A rule a pending transit time milestone must pass. Check returns its problems, or none. */
interface PendingTransitTimeMilestoneRule {
    fun check(item: PendingTransitTimeMilestone): MutableList<String>
}

data class PendingTransitTimeMilestoneChannelLengthRule(var maxLength: Int) : PendingTransitTimeMilestoneRule {
    override fun check(item: PendingTransitTimeMilestone): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeMilestoneQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PendingTransitTimeMilestoneRule {
    override fun check(item: PendingTransitTimeMilestone): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class PendingTransitTimeMilestoneRequiredCodeRule(var message: String) : PendingTransitTimeMilestoneRule {
    override fun check(item: PendingTransitTimeMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeMilestoneCarrierLengthRule(var maxLength: Int) : PendingTransitTimeMilestoneRule {
    override fun check(item: PendingTransitTimeMilestone): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeMilestoneAllowedChannelRule(
    var allowed: MutableList<String>,
) : PendingTransitTimeMilestoneRule {
    override fun check(item: PendingTransitTimeMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class PendingTransitTimeMilestoneRequiredNameRule(var message: String) : PendingTransitTimeMilestoneRule {
    override fun check(item: PendingTransitTimeMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending transit time milestone. */
data class PendingTransitTimeMilestoneValidator(var rules: MutableList<PendingTransitTimeMilestoneRule>) {
    fun validate(item: PendingTransitTimeMilestone): MutableList<String> {
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

    fun requireValid(item: PendingTransitTimeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTransitTimeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTransitTimeMilestoneValidator(): PendingTransitTimeMilestoneValidator {
    return PendingTransitTimeMilestoneValidator(
        mutableListOf<PendingTransitTimeMilestoneRule>(
            PendingTransitTimeMilestoneChannelLengthRule(16),
            PendingTransitTimeMilestoneQuantityRangeRule(255, 3600),
            PendingTransitTimeMilestoneRequiredCodeRule("pending transit time milestone needs this field"),
            PendingTransitTimeMilestoneCarrierLengthRule(1000),
            PendingTransitTimeMilestoneAllowedChannelRule(
                mutableListOf<String>("fallback", "adaptive", "batched"),
            ),
            PendingTransitTimeMilestoneRequiredNameRule("pending transit time milestone needs a value"),
        ),
    )
}
