package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize

/** A rule a rolling service plan tier must pass. Check returns its problems, or none. */
interface RollingServicePlanTierRule {
    fun check(item: RollingServicePlanTier): MutableList<String>
}

data class RollingServicePlanTierCodeLengthRule(var maxLength: Int) : RollingServicePlanTierRule {
    override fun check(item: RollingServicePlanTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingServicePlanTierAllowedCodeRule(
    var allowed: MutableList<String>,
) : RollingServicePlanTierRule {
    override fun check(item: RollingServicePlanTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class RollingServicePlanTierTitleLengthRule(var maxLength: Int) : RollingServicePlanTierRule {
    override fun check(item: RollingServicePlanTier): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling service plan tier. */
data class RollingServicePlanTierValidator(var rules: MutableList<RollingServicePlanTierRule>) {
    fun validate(item: RollingServicePlanTier): MutableList<String> {
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

    fun requireValid(item: RollingServicePlanTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingServicePlanTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingServicePlanTierValidator(): RollingServicePlanTierValidator {
    return RollingServicePlanTierValidator(
        mutableListOf<RollingServicePlanTierRule>(
            RollingServicePlanTierCodeLengthRule(32),
            RollingServicePlanTierAllowedCodeRule(mutableListOf<String>()),
            RollingServicePlanTierTitleLengthRule(64),
        ),
    )
}
