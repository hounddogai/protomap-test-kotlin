package com.acme.transport.customs.inspection.inspectionestimate

/** A rule a legacy inspection estimate must pass. Check returns its problems, or none. */
interface LegacyInspectionEstimateRule {
    fun check(item: LegacyInspectionEstimate): MutableList<String>
}

data class LegacyInspectionEstimateNameLengthRule(var maxLength: Int) : LegacyInspectionEstimateRule {
    override fun check(item: LegacyInspectionEstimate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionEstimateCodeLengthRule(var maxLength: Int) : LegacyInspectionEstimateRule {
    override fun check(item: LegacyInspectionEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionEstimateCostRangeRule(
    var min: Double,
    var max: Double,
) : LegacyInspectionEstimateRule {
    override fun check(item: LegacyInspectionEstimate): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyInspectionEstimateUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyInspectionEstimateRule {
    override fun check(item: LegacyInspectionEstimate): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy inspection estimate. */
data class LegacyInspectionEstimateValidator(var rules: MutableList<LegacyInspectionEstimateRule>) {
    fun validate(item: LegacyInspectionEstimate): MutableList<String> {
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

    fun requireValid(item: LegacyInspectionEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyInspectionEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyInspectionEstimateValidator(): LegacyInspectionEstimateValidator {
    return LegacyInspectionEstimateValidator(
        mutableListOf<LegacyInspectionEstimateRule>(
            LegacyInspectionEstimateNameLengthRule(1000),
            LegacyInspectionEstimateCodeLengthRule(1000),
            LegacyInspectionEstimateCostRangeRule(3.7, 3477.4),
            LegacyInspectionEstimateUpdatedAtRangeRule(80, 120),
        ),
    )
}
