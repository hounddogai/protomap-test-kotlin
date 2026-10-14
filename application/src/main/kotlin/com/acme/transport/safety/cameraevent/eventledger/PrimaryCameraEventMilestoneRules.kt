package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.text.normalize

/** A rule a primary camera event milestone must pass. Check returns its problems, or none. */
interface PrimaryCameraEventMilestoneRule {
    fun check(item: PrimaryCameraEventMilestone): MutableList<String>
}

data class PrimaryCameraEventMilestoneLimitRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryCameraEventMilestoneRule {
    override fun check(item: PrimaryCameraEventMilestone): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventMilestoneRequiredTitleRule(var message: String) : PrimaryCameraEventMilestoneRule {
    override fun check(item: PrimaryCameraEventMilestone): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventMilestoneRequiredCodeRule(var message: String) : PrimaryCameraEventMilestoneRule {
    override fun check(item: PrimaryCameraEventMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventMilestoneAllowedTitleRule(
    var allowed: MutableList<String>,
) : PrimaryCameraEventMilestoneRule {
    override fun check(item: PrimaryCameraEventMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCameraEventMilestoneCodePrefixRule(var prefix: String) : PrimaryCameraEventMilestoneRule {
    override fun check(item: PrimaryCameraEventMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary camera event milestone. */
data class PrimaryCameraEventMilestoneValidator(var rules: MutableList<PrimaryCameraEventMilestoneRule>) {
    fun validate(item: PrimaryCameraEventMilestone): MutableList<String> {
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

    fun requireValid(item: PrimaryCameraEventMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCameraEventMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCameraEventMilestoneValidator(): PrimaryCameraEventMilestoneValidator {
    return PrimaryCameraEventMilestoneValidator(
        mutableListOf<PrimaryCameraEventMilestoneRule>(
            PrimaryCameraEventMilestoneLimitRangeRule(32, 86400),
            PrimaryCameraEventMilestoneRequiredTitleRule("primary camera event milestone needs an entry"),
            PrimaryCameraEventMilestoneRequiredCodeRule("primary camera event milestone needs this field"),
            PrimaryCameraEventMilestoneAllowedTitleRule(mutableListOf<String>("lenient")),
            PrimaryCameraEventMilestoneCodePrefixRule("OPS"),
        ),
    )
}
