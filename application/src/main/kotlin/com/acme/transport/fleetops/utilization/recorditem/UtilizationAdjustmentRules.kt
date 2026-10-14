package com.acme.transport.fleetops.utilization.recorditem

/** A rule a utilization adjustment must pass. Check returns its problems, or none. */
interface UtilizationAdjustmentRule {
    fun check(item: UtilizationAdjustment): MutableList<String>
}

data class UtilizationAdjustmentFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : UtilizationAdjustmentRule {
    override fun check(item: UtilizationAdjustment): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class UtilizationAdjustmentRequiredCodeRule(var message: String) : UtilizationAdjustmentRule {
    override fun check(item: UtilizationAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAdjustmentLaneLengthRule(var maxLength: Int) : UtilizationAdjustmentRule {
    override fun check(item: UtilizationAdjustment): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAdjustmentRequiredLaneRule(var message: String) : UtilizationAdjustmentRule {
    override fun check(item: UtilizationAdjustment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization adjustment. */
data class UtilizationAdjustmentValidator(var rules: MutableList<UtilizationAdjustmentRule>) {
    fun validate(item: UtilizationAdjustment): MutableList<String> {
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

    fun requireValid(item: UtilizationAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationAdjustmentValidator(): UtilizationAdjustmentValidator {
    return UtilizationAdjustmentValidator(
        mutableListOf<UtilizationAdjustmentRule>(
            UtilizationAdjustmentFuelLitersRangeRule(9.3, 3944.2),
            UtilizationAdjustmentRequiredCodeRule("utilization adjustment needs an entry"),
            UtilizationAdjustmentLaneLengthRule(64),
            UtilizationAdjustmentRequiredLaneRule("utilization adjustment needs an entry"),
        ),
    )
}
