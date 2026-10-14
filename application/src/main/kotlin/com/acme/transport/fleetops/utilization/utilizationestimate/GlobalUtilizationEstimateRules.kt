package com.acme.transport.fleetops.utilization.utilizationestimate

/** A rule a global utilization estimate must pass. Check returns its problems, or none. */
interface GlobalUtilizationEstimateRule {
    fun check(item: GlobalUtilizationEstimate): MutableList<String>
}

data class GlobalUtilizationEstimateTitleLengthRule(var maxLength: Int) : GlobalUtilizationEstimateRule {
    override fun check(item: GlobalUtilizationEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationEstimateCodeLengthRule(var maxLength: Int) : GlobalUtilizationEstimateRule {
    override fun check(item: GlobalUtilizationEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationEstimateDepotLengthRule(var maxLength: Int) : GlobalUtilizationEstimateRule {
    override fun check(item: GlobalUtilizationEstimate): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalUtilizationEstimateExpiresAtRangeRule(
    var min: Int,
    var max: Int,
) : GlobalUtilizationEstimateRule {
    override fun check(item: GlobalUtilizationEstimate): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class GlobalUtilizationEstimateDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : GlobalUtilizationEstimateRule {
    override fun check(item: GlobalUtilizationEstimate): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global utilization estimate. */
data class GlobalUtilizationEstimateValidator(var rules: MutableList<GlobalUtilizationEstimateRule>) {
    fun validate(item: GlobalUtilizationEstimate): MutableList<String> {
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

    fun requireValid(item: GlobalUtilizationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalUtilizationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalUtilizationEstimateValidator(): GlobalUtilizationEstimateValidator {
    return GlobalUtilizationEstimateValidator(
        mutableListOf<GlobalUtilizationEstimateRule>(
            GlobalUtilizationEstimateTitleLengthRule(32),
            GlobalUtilizationEstimateCodeLengthRule(3600),
            GlobalUtilizationEstimateDepotLengthRule(255),
            GlobalUtilizationEstimateExpiresAtRangeRule(1000, 86400),
            GlobalUtilizationEstimateDurationMinutesRangeRule(32, 255),
        ),
    )
}
