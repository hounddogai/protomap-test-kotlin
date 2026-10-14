package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.normalize

/** A rule a tiered inspection group must pass. Check returns its problems, or none. */
interface TieredInspectionGroupRule {
    fun check(item: TieredInspectionGroup): MutableList<String>
}

data class TieredInspectionGroupRequiredRegionRule(var message: String) : TieredInspectionGroupRule {
    override fun check(item: TieredInspectionGroup): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredInspectionGroupEtaMinutesRangeRule(var min: Int, var max: Int) : TieredInspectionGroupRule {
    override fun check(item: TieredInspectionGroup): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class TieredInspectionGroupAllowedRegionRule(
    var allowed: MutableList<String>,
) : TieredInspectionGroupRule {
    override fun check(item: TieredInspectionGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered inspection group. */
data class TieredInspectionGroupValidator(var rules: MutableList<TieredInspectionGroupRule>) {
    fun validate(item: TieredInspectionGroup): MutableList<String> {
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

    fun requireValid(item: TieredInspectionGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredInspectionGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredInspectionGroupValidator(): TieredInspectionGroupValidator {
    return TieredInspectionGroupValidator(
        mutableListOf<TieredInspectionGroupRule>(
            TieredInspectionGroupRequiredRegionRule("tiered inspection group needs this field"),
            TieredInspectionGroupEtaMinutesRangeRule(80, 255),
            TieredInspectionGroupAllowedRegionRule(mutableListOf<String>()),
        ),
    )
}
