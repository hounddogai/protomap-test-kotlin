package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.kit.text.normalize

/** A rule a seasonal utilization policy must pass. Check returns its problems, or none. */
interface SeasonalUtilizationPolicyRule {
    fun check(item: SeasonalUtilizationPolicy): MutableList<String>
}

data class SeasonalUtilizationPolicyPriorityRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalUtilizationPolicyRule {
    override fun check(item: SeasonalUtilizationPolicy): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class SeasonalUtilizationPolicyBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalUtilizationPolicyRule {
    override fun check(item: SeasonalUtilizationPolicy): MutableList<String> {
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

data class SeasonalUtilizationPolicyRequiredSegmentRule(var message: String) : SeasonalUtilizationPolicyRule {
    override fun check(item: SeasonalUtilizationPolicy): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationPolicyVehicleIdLengthRule(var maxLength: Int) : SeasonalUtilizationPolicyRule {
    override fun check(item: SeasonalUtilizationPolicy): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalUtilizationPolicyAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : SeasonalUtilizationPolicyRule {
    override fun check(item: SeasonalUtilizationPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is out of range")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal utilization policy. */
data class SeasonalUtilizationPolicyValidator(var rules: MutableList<SeasonalUtilizationPolicyRule>) {
    fun validate(item: SeasonalUtilizationPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalUtilizationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalUtilizationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalUtilizationPolicyValidator(): SeasonalUtilizationPolicyValidator {
    return SeasonalUtilizationPolicyValidator(
        mutableListOf<SeasonalUtilizationPolicyRule>(
            SeasonalUtilizationPolicyPriorityRangeRule(64, 3600),
            SeasonalUtilizationPolicyBatchSizeRangeRule(80, 120),
            SeasonalUtilizationPolicyRequiredSegmentRule("seasonal utilization policy needs this field"),
            SeasonalUtilizationPolicyVehicleIdLengthRule(120),
            SeasonalUtilizationPolicyAllowedVehicleIdRule(mutableListOf<String>("rolling")),
        ),
    )
}
