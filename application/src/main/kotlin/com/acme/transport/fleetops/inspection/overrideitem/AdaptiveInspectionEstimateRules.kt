package com.acme.transport.fleetops.inspection.overrideitem

/** A rule a adaptive inspection estimate must pass. Check returns its problems, or none. */
interface AdaptiveInspectionEstimateRule {
    fun check(item: AdaptiveInspectionEstimate): MutableList<String>
}

data class AdaptiveInspectionEstimateTitleLengthRule(var maxLength: Int) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionEstimateMarginRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionEstimateFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionEstimateRegionLengthRule(var maxLength: Int) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionEstimateCodePrefixRule(var prefix: String) : AdaptiveInspectionEstimateRule {
    override fun check(item: AdaptiveInspectionEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection estimate. */
data class AdaptiveInspectionEstimateValidator(var rules: MutableList<AdaptiveInspectionEstimateRule>) {
    fun validate(item: AdaptiveInspectionEstimate): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionEstimateValidator(): AdaptiveInspectionEstimateValidator {
    return AdaptiveInspectionEstimateValidator(
        mutableListOf<AdaptiveInspectionEstimateRule>(
            AdaptiveInspectionEstimateTitleLengthRule(16),
            AdaptiveInspectionEstimateMarginRangeRule(0.1, 1396.2),
            AdaptiveInspectionEstimateFloorValueRangeRule(2.5, 1044.1),
            AdaptiveInspectionEstimateRegionLengthRule(1000),
            AdaptiveInspectionEstimateCodePrefixRule("OPS"),
        ),
    )
}
