package com.acme.transport.fleetops.utilization.overrideitem

/** A rule a utilization variance must pass. Check returns its problems, or none. */
interface UtilizationVarianceRule {
    fun check(item: UtilizationVariance): MutableList<String>
}

data class UtilizationVarianceWindowDaysRangeRule(var min: Int, var max: Int) : UtilizationVarianceRule {
    override fun check(item: UtilizationVariance): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class UtilizationVarianceLimitRangeRule(var min: Int, var max: Int) : UtilizationVarianceRule {
    override fun check(item: UtilizationVariance): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationVarianceStopCountRangeRule(var min: Int, var max: Int) : UtilizationVarianceRule {
    override fun check(item: UtilizationVariance): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationVarianceCodePrefixRule(var prefix: String) : UtilizationVarianceRule {
    override fun check(item: UtilizationVariance): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization variance. */
data class UtilizationVarianceValidator(var rules: MutableList<UtilizationVarianceRule>) {
    fun validate(item: UtilizationVariance): MutableList<String> {
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

    fun requireValid(item: UtilizationVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationVarianceValidator(): UtilizationVarianceValidator {
    return UtilizationVarianceValidator(
        mutableListOf<UtilizationVarianceRule>(
            UtilizationVarianceWindowDaysRangeRule(3600, 86400),
            UtilizationVarianceLimitRangeRule(1000, 3600),
            UtilizationVarianceStopCountRangeRule(64, 1000),
            UtilizationVarianceCodePrefixRule("REF"),
        ),
    )
}
