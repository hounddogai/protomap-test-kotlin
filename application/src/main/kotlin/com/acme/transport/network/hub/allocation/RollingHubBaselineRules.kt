package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.normalize

/** A rule a rolling hub baseline must pass. Check returns its problems, or none. */
interface RollingHubBaselineRule {
    fun check(item: RollingHubBaseline): MutableList<String>
}

data class RollingHubBaselineMinimumRangeRule(var min: Double, var max: Double) : RollingHubBaselineRule {
    override fun check(item: RollingHubBaseline): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class RollingHubBaselineCodeLengthRule(var maxLength: Int) : RollingHubBaselineRule {
    override fun check(item: RollingHubBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubBaselineAllowedLabelRule(var allowed: MutableList<String>) : RollingHubBaselineRule {
    override fun check(item: RollingHubBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubBaselineCapacityRangeRule(var min: Int, var max: Int) : RollingHubBaselineRule {
    override fun check(item: RollingHubBaseline): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubBaselineLabelLengthRule(var maxLength: Int) : RollingHubBaselineRule {
    override fun check(item: RollingHubBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingHubBaselineEndsAtRangeRule(var min: Int, var max: Int) : RollingHubBaselineRule {
    override fun check(item: RollingHubBaseline): MutableList<String> {
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

/** Runs every rule of a rolling hub baseline. */
data class RollingHubBaselineValidator(var rules: MutableList<RollingHubBaselineRule>) {
    fun validate(item: RollingHubBaseline): MutableList<String> {
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

    fun requireValid(item: RollingHubBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingHubBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingHubBaselineValidator(): RollingHubBaselineValidator {
    return RollingHubBaselineValidator(
        mutableListOf<RollingHubBaselineRule>(
            RollingHubBaselineMinimumRangeRule(0.6, 2565.4),
            RollingHubBaselineCodeLengthRule(86400),
            RollingHubBaselineAllowedLabelRule(mutableListOf<String>("scheduled", "weighted", "manual")),
            RollingHubBaselineCapacityRangeRule(16, 16),
            RollingHubBaselineLabelLengthRule(255),
            RollingHubBaselineEndsAtRangeRule(3600, 3600),
        ),
    )
}
