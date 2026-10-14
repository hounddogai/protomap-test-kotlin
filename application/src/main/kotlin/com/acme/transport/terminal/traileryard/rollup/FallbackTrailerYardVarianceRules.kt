package com.acme.transport.terminal.traileryard.rollup

/** A rule a fallback trailer yard variance must pass. Check returns its problems, or none. */
interface FallbackTrailerYardVarianceRule {
    fun check(item: FallbackTrailerYardVariance): MutableList<String>
}

data class FallbackTrailerYardVarianceRequiredLaneRule(var message: String) : FallbackTrailerYardVarianceRule {
    override fun check(item: FallbackTrailerYardVariance): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardVarianceBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : FallbackTrailerYardVarianceRule {
    override fun check(item: FallbackTrailerYardVariance): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerYardVarianceRequiredCodeRule(var message: String) : FallbackTrailerYardVarianceRule {
    override fun check(item: FallbackTrailerYardVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback trailer yard variance. */
data class FallbackTrailerYardVarianceValidator(var rules: MutableList<FallbackTrailerYardVarianceRule>) {
    fun validate(item: FallbackTrailerYardVariance): MutableList<String> {
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

    fun requireValid(item: FallbackTrailerYardVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTrailerYardVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTrailerYardVarianceValidator(): FallbackTrailerYardVarianceValidator {
    return FallbackTrailerYardVarianceValidator(
        mutableListOf<FallbackTrailerYardVarianceRule>(
            FallbackTrailerYardVarianceRequiredLaneRule("fallback trailer yard variance needs a value"),
            FallbackTrailerYardVarianceBatchSizeRangeRule(16, 1000),
            FallbackTrailerYardVarianceRequiredCodeRule("fallback trailer yard variance needs an entry"),
        ),
    )
}
