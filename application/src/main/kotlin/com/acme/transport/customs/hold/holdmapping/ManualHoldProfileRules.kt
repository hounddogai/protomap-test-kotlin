package com.acme.transport.customs.hold.holdmapping

/** A rule a manual hold profile must pass. Check returns its problems, or none. */
interface ManualHoldProfileRule {
    fun check(item: ManualHoldProfile): MutableList<String>
}

data class ManualHoldProfileCostRangeRule(var min: Double, var max: Double) : ManualHoldProfileRule {
    override fun check(item: ManualHoldProfile): MutableList<String> {
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

data class ManualHoldProfileDistanceKmRangeRule(var min: Double, var max: Double) : ManualHoldProfileRule {
    override fun check(item: ManualHoldProfile): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class ManualHoldProfileFloorValueRangeRule(var min: Double, var max: Double) : ManualHoldProfileRule {
    override fun check(item: ManualHoldProfile): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualHoldProfileRequiredLabelRule(var message: String) : ManualHoldProfileRule {
    override fun check(item: ManualHoldProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual hold profile. */
data class ManualHoldProfileValidator(var rules: MutableList<ManualHoldProfileRule>) {
    fun validate(item: ManualHoldProfile): MutableList<String> {
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

    fun requireValid(item: ManualHoldProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualHoldProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultManualHoldProfileValidator(): ManualHoldProfileValidator {
    return ManualHoldProfileValidator(
        mutableListOf<ManualHoldProfileRule>(
            ManualHoldProfileCostRangeRule(6.9, 1109.9),
            ManualHoldProfileDistanceKmRangeRule(9.8, 1470.5),
            ManualHoldProfileFloorValueRangeRule(9.9, 3745.5),
            ManualHoldProfileRequiredLabelRule("manual hold profile needs this field"),
        ),
    )
}
