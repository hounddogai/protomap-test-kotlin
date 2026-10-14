package com.acme.transport.linehaul.capacity.threshold

/** A rule a fallback capacity bundle must pass. Check returns its problems, or none. */
interface FallbackCapacityBundleRule {
    fun check(item: FallbackCapacityBundle): MutableList<String>
}

data class FallbackCapacityBundleEtaMinutesRangeRule(var min: Int, var max: Int) : FallbackCapacityBundleRule {
    override fun check(item: FallbackCapacityBundle): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityBundleRatioRangeRule(var min: Double, var max: Double) : FallbackCapacityBundleRule {
    override fun check(item: FallbackCapacityBundle): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCapacityBundleRequiredSourceRule(var message: String) : FallbackCapacityBundleRule {
    override fun check(item: FallbackCapacityBundle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback capacity bundle. */
data class FallbackCapacityBundleValidator(var rules: MutableList<FallbackCapacityBundleRule>) {
    fun validate(item: FallbackCapacityBundle): MutableList<String> {
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

    fun requireValid(item: FallbackCapacityBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCapacityBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCapacityBundleValidator(): FallbackCapacityBundleValidator {
    return FallbackCapacityBundleValidator(
        mutableListOf<FallbackCapacityBundleRule>(
            FallbackCapacityBundleEtaMinutesRangeRule(32, 255),
            FallbackCapacityBundleRatioRangeRule(9.8, 2683.6),
            FallbackCapacityBundleRequiredSourceRule("fallback capacity bundle needs a value"),
        ),
    )
}
