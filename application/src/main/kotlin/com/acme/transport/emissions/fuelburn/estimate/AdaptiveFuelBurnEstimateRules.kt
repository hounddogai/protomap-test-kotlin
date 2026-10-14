package com.acme.transport.emissions.fuelburn.estimate

/** A rule a adaptive fuel burn estimate must pass. Check returns its problems, or none. */
interface AdaptiveFuelBurnEstimateRule {
    fun check(item: AdaptiveFuelBurnEstimate): MutableList<String>
}

data class AdaptiveFuelBurnEstimateRequiredSourceRule(var message: String) : AdaptiveFuelBurnEstimateRule {
    override fun check(item: AdaptiveFuelBurnEstimate): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnEstimateSourceLengthRule(var maxLength: Int) : AdaptiveFuelBurnEstimateRule {
    override fun check(item: AdaptiveFuelBurnEstimate): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnEstimateRequiredCodeRule(var message: String) : AdaptiveFuelBurnEstimateRule {
    override fun check(item: AdaptiveFuelBurnEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnEstimateCodePrefixRule(var prefix: String) : AdaptiveFuelBurnEstimateRule {
    override fun check(item: AdaptiveFuelBurnEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveFuelBurnEstimateRatioRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveFuelBurnEstimateRule {
    override fun check(item: AdaptiveFuelBurnEstimate): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive fuel burn estimate. */
data class AdaptiveFuelBurnEstimateValidator(var rules: MutableList<AdaptiveFuelBurnEstimateRule>) {
    fun validate(item: AdaptiveFuelBurnEstimate): MutableList<String> {
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

    fun requireValid(item: AdaptiveFuelBurnEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveFuelBurnEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveFuelBurnEstimateValidator(): AdaptiveFuelBurnEstimateValidator {
    return AdaptiveFuelBurnEstimateValidator(
        mutableListOf<AdaptiveFuelBurnEstimateRule>(
            AdaptiveFuelBurnEstimateRequiredSourceRule("adaptive fuel burn estimate needs this field"),
            AdaptiveFuelBurnEstimateSourceLengthRule(32),
            AdaptiveFuelBurnEstimateRequiredCodeRule("adaptive fuel burn estimate needs a value"),
            AdaptiveFuelBurnEstimateCodePrefixRule("OPS"),
            AdaptiveFuelBurnEstimateRatioRangeRule(0.1, 3093.9),
        ),
    )
}
