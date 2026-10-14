package com.acme.transport.fleetops.utilization.defaultutilizationsummary

/** A rule a rolling utilization score must pass. Check returns its problems, or none. */
interface RollingUtilizationScoreRule {
    fun check(item: RollingUtilizationScore): MutableList<String>
}

data class RollingUtilizationScoreFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : RollingUtilizationScoreRule {
    override fun check(item: RollingUtilizationScore): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationScoreRequiredCodeRule(var message: String) : RollingUtilizationScoreRule {
    override fun check(item: RollingUtilizationScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationScoreCapacityRangeRule(var min: Int, var max: Int) : RollingUtilizationScoreRule {
    override fun check(item: RollingUtilizationScore): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingUtilizationScoreCodePrefixRule(var prefix: String) : RollingUtilizationScoreRule {
    override fun check(item: RollingUtilizationScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling utilization score. */
data class RollingUtilizationScoreValidator(var rules: MutableList<RollingUtilizationScoreRule>) {
    fun validate(item: RollingUtilizationScore): MutableList<String> {
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

    fun requireValid(item: RollingUtilizationScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingUtilizationScore>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingUtilizationScoreValidator(): RollingUtilizationScoreValidator {
    return RollingUtilizationScoreValidator(
        mutableListOf<RollingUtilizationScoreRule>(
            RollingUtilizationScoreFloorValueRangeRule(9.8, 398.9),
            RollingUtilizationScoreRequiredCodeRule("rolling utilization score needs a value"),
            RollingUtilizationScoreCapacityRangeRule(64, 64),
            RollingUtilizationScoreCodePrefixRule("OPS"),
        ),
    )
}
