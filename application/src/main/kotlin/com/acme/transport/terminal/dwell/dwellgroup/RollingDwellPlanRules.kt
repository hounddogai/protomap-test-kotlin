package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.text.normalize

/** A rule a rolling dwell plan must pass. Check returns its problems, or none. */
interface RollingDwellPlanRule {
    fun check(item: RollingDwellPlan): MutableList<String>
}

data class RollingDwellPlanUpdatedAtRangeRule(var min: Int, var max: Int) : RollingDwellPlanRule {
    override fun check(item: RollingDwellPlan): MutableList<String> {
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

data class RollingDwellPlanLaneLengthRule(var maxLength: Int) : RollingDwellPlanRule {
    override fun check(item: RollingDwellPlan): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellPlanCodeLengthRule(var maxLength: Int) : RollingDwellPlanRule {
    override fun check(item: RollingDwellPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellPlanMarginRangeRule(var min: Double, var max: Double) : RollingDwellPlanRule {
    override fun check(item: RollingDwellPlan): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class RollingDwellPlanRequiredSourceRule(var message: String) : RollingDwellPlanRule {
    override fun check(item: RollingDwellPlan): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellPlanAllowedChecksumRule(var allowed: MutableList<String>) : RollingDwellPlanRule {
    override fun check(item: RollingDwellPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.checksum))) {
            return mutableListOf<String>("checksum ${item.checksum} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dwell plan. */
data class RollingDwellPlanValidator(var rules: MutableList<RollingDwellPlanRule>) {
    fun validate(item: RollingDwellPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDwellPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDwellPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDwellPlanValidator(): RollingDwellPlanValidator {
    return RollingDwellPlanValidator(
        mutableListOf<RollingDwellPlanRule>(
            RollingDwellPlanUpdatedAtRangeRule(32, 80),
            RollingDwellPlanLaneLengthRule(255),
            RollingDwellPlanCodeLengthRule(32),
            RollingDwellPlanMarginRangeRule(6.0, 4984.3),
            RollingDwellPlanRequiredSourceRule("rolling dwell plan needs this field"),
            RollingDwellPlanAllowedChecksumRule(mutableListOf<String>()),
        ),
    )
}
