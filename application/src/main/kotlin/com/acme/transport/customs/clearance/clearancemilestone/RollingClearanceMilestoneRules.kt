package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.text.normalize

/** A rule a rolling clearance milestone must pass. Check returns its problems, or none. */
interface RollingClearanceMilestoneRule {
    fun check(item: RollingClearanceMilestone): MutableList<String>
}

data class RollingClearanceMilestoneRateRangeRule(
    var min: Double,
    var max: Double,
) : RollingClearanceMilestoneRule {
    override fun check(item: RollingClearanceMilestone): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingClearanceMilestoneBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : RollingClearanceMilestoneRule {
    override fun check(item: RollingClearanceMilestone): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RollingClearanceMilestoneRequiredLabelRule(var message: String) : RollingClearanceMilestoneRule {
    override fun check(item: RollingClearanceMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingClearanceMilestoneLabelLengthRule(var maxLength: Int) : RollingClearanceMilestoneRule {
    override fun check(item: RollingClearanceMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingClearanceMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingClearanceMilestoneRule {
    override fun check(item: RollingClearanceMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RollingClearanceMilestoneRequiredCodeRule(var message: String) : RollingClearanceMilestoneRule {
    override fun check(item: RollingClearanceMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling clearance milestone. */
data class RollingClearanceMilestoneValidator(var rules: MutableList<RollingClearanceMilestoneRule>) {
    fun validate(item: RollingClearanceMilestone): MutableList<String> {
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

    fun requireValid(item: RollingClearanceMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingClearanceMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingClearanceMilestoneValidator(): RollingClearanceMilestoneValidator {
    return RollingClearanceMilestoneValidator(
        mutableListOf<RollingClearanceMilestoneRule>(
            RollingClearanceMilestoneRateRangeRule(0.5, 160.7),
            RollingClearanceMilestoneBatchSizeRangeRule(255, 3600),
            RollingClearanceMilestoneRequiredLabelRule("rolling clearance milestone needs a value"),
            RollingClearanceMilestoneLabelLengthRule(80),
            RollingClearanceMilestoneAllowedCodeRule(mutableListOf<String>("batched", "regional")),
            RollingClearanceMilestoneRequiredCodeRule("rolling clearance milestone needs this field"),
        ),
    )
}
