package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.normalize

/** A rule a rolling fuel burn milestone must pass. Check returns its problems, or none. */
interface RollingFuelBurnMilestoneRule {
    fun check(item: RollingFuelBurnMilestone): MutableList<String>
}

data class RollingFuelBurnMilestoneRequiredPhoneRule(var message: String) : RollingFuelBurnMilestoneRule {
    override fun check(item: RollingFuelBurnMilestone): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnMilestoneUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : RollingFuelBurnMilestoneRule {
    override fun check(item: RollingFuelBurnMilestone): MutableList<String> {
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

data class RollingFuelBurnMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingFuelBurnMilestoneRule {
    override fun check(item: RollingFuelBurnMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnMilestoneCarrierLengthRule(var maxLength: Int) : RollingFuelBurnMilestoneRule {
    override fun check(item: RollingFuelBurnMilestone): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnMilestoneTitleLengthRule(var maxLength: Int) : RollingFuelBurnMilestoneRule {
    override fun check(item: RollingFuelBurnMilestone): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingFuelBurnMilestoneCodeLengthRule(var maxLength: Int) : RollingFuelBurnMilestoneRule {
    override fun check(item: RollingFuelBurnMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling fuel burn milestone. */
data class RollingFuelBurnMilestoneValidator(var rules: MutableList<RollingFuelBurnMilestoneRule>) {
    fun validate(item: RollingFuelBurnMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingFuelBurnMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingFuelBurnMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingFuelBurnMilestoneValidator(): RollingFuelBurnMilestoneValidator {
    return RollingFuelBurnMilestoneValidator(
        mutableListOf<RollingFuelBurnMilestoneRule>(
            RollingFuelBurnMilestoneRequiredPhoneRule("rolling fuel burn milestone needs a value"),
            RollingFuelBurnMilestoneUpdatedAtRangeRule(255, 1000),
            RollingFuelBurnMilestoneAllowedCodeRule(mutableListOf<String>("global", "scheduled")),
            RollingFuelBurnMilestoneCarrierLengthRule(120),
            RollingFuelBurnMilestoneTitleLengthRule(16),
            RollingFuelBurnMilestoneCodeLengthRule(80),
        ),
    )
}
