package com.acme.transport.fleetops.utilization.segment

/** A rule a regional utilization segment must pass. Check returns its problems, or none. */
interface RegionalUtilizationSegmentRule {
    fun check(item: RegionalUtilizationSegment): MutableList<String>
}

data class RegionalUtilizationSegmentCostRangeRule(
    var min: Double,
    var max: Double,
) : RegionalUtilizationSegmentRule {
    override fun check(item: RegionalUtilizationSegment): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class RegionalUtilizationSegmentPalletsRangeRule(
    var min: Int,
    var max: Int,
) : RegionalUtilizationSegmentRule {
    override fun check(item: RegionalUtilizationSegment): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalUtilizationSegmentRequiredLabelRule(var message: String) : RegionalUtilizationSegmentRule {
    override fun check(item: RegionalUtilizationSegment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional utilization segment. */
data class RegionalUtilizationSegmentValidator(var rules: MutableList<RegionalUtilizationSegmentRule>) {
    fun validate(item: RegionalUtilizationSegment): MutableList<String> {
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

    fun requireValid(item: RegionalUtilizationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalUtilizationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalUtilizationSegmentValidator(): RegionalUtilizationSegmentValidator {
    return RegionalUtilizationSegmentValidator(
        mutableListOf<RegionalUtilizationSegmentRule>(
            RegionalUtilizationSegmentCostRangeRule(8.3, 4915.7),
            RegionalUtilizationSegmentPalletsRangeRule(1000, 86400),
            RegionalUtilizationSegmentRequiredLabelRule("regional utilization segment needs this field"),
        ),
    )
}
