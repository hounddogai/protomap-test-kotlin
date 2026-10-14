package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.kit.text.normalize

/** A rule a drop summary must pass. Check returns its problems, or none. */
interface DropSummaryRule {
    fun check(item: DropSummary): MutableList<String>
}

data class DropSummaryAllowedChannelRule(var allowed: MutableList<String>) : DropSummaryRule {
    override fun check(item: DropSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.channel))) {
            return mutableListOf<String>("channel ${item.channel} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DropSummaryRequiredChannelRule(var message: String) : DropSummaryRule {
    override fun check(item: DropSummary): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropSummaryQuantityRangeRule(var min: Int, var max: Int) : DropSummaryRule {
    override fun check(item: DropSummary): MutableList<String> {
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

data class DropSummaryRequiredLaneRule(var message: String) : DropSummaryRule {
    override fun check(item: DropSummary): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropSummaryLaneLengthRule(var maxLength: Int) : DropSummaryRule {
    override fun check(item: DropSummary): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop summary. */
data class DropSummaryValidator(var rules: MutableList<DropSummaryRule>) {
    fun validate(item: DropSummary): MutableList<String> {
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

    fun requireValid(item: DropSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultDropSummaryValidator(): DropSummaryValidator {
    return DropSummaryValidator(
        mutableListOf<DropSummaryRule>(
            DropSummaryAllowedChannelRule(mutableListOf<String>("legacy", "global", "fallback")),
            DropSummaryRequiredChannelRule("drop summary needs a value"),
            DropSummaryQuantityRangeRule(3600, 86400),
            DropSummaryRequiredLaneRule("drop summary needs a value"),
            DropSummaryLaneLengthRule(64),
        ),
    )
}
