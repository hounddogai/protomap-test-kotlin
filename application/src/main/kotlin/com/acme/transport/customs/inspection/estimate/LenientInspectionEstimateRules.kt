package com.acme.transport.customs.inspection.estimate

/** A rule a lenient inspection estimate must pass. Check returns its problems, or none. */
interface LenientInspectionEstimateRule {
    fun check(item: LenientInspectionEstimate): MutableList<String>
}

data class LenientInspectionEstimateLocaleLengthRule(var maxLength: Int) : LenientInspectionEstimateRule {
    override fun check(item: LenientInspectionEstimate): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionEstimateCapacityRangeRule(
    var min: Int,
    var max: Int,
) : LenientInspectionEstimateRule {
    override fun check(item: LenientInspectionEstimate): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LenientInspectionEstimateWeightRangeRule(
    var min: Double,
    var max: Double,
) : LenientInspectionEstimateRule {
    override fun check(item: LenientInspectionEstimate): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientInspectionEstimateTitleLengthRule(var maxLength: Int) : LenientInspectionEstimateRule {
    override fun check(item: LenientInspectionEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient inspection estimate. */
data class LenientInspectionEstimateValidator(var rules: MutableList<LenientInspectionEstimateRule>) {
    fun validate(item: LenientInspectionEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientInspectionEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientInspectionEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientInspectionEstimateValidator(): LenientInspectionEstimateValidator {
    return LenientInspectionEstimateValidator(
        mutableListOf<LenientInspectionEstimateRule>(
            LenientInspectionEstimateLocaleLengthRule(80),
            LenientInspectionEstimateCapacityRangeRule(16, 32),
            LenientInspectionEstimateWeightRangeRule(0.5, 786.9),
            LenientInspectionEstimateTitleLengthRule(3600),
        ),
    )
}
