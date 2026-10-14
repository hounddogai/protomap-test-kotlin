package com.acme.transport.terminal.crossdock.cappeddockestimate

/** A rule a capped cross dock estimate must pass. Check returns its problems, or none. */
interface CappedCrossDockEstimateRule {
    fun check(item: CappedCrossDockEstimate): MutableList<String>
}

data class CappedCrossDockEstimateCodePrefixRule(var prefix: String) : CappedCrossDockEstimateRule {
    override fun check(item: CappedCrossDockEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockEstimateTitleLengthRule(var maxLength: Int) : CappedCrossDockEstimateRule {
    override fun check(item: CappedCrossDockEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCrossDockEstimateWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : CappedCrossDockEstimateRule {
    override fun check(item: CappedCrossDockEstimate): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped cross dock estimate. */
data class CappedCrossDockEstimateValidator(var rules: MutableList<CappedCrossDockEstimateRule>) {
    fun validate(item: CappedCrossDockEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCrossDockEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCrossDockEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCrossDockEstimateValidator(): CappedCrossDockEstimateValidator {
    return CappedCrossDockEstimateValidator(
        mutableListOf<CappedCrossDockEstimateRule>(
            CappedCrossDockEstimateCodePrefixRule("REF"),
            CappedCrossDockEstimateTitleLengthRule(1000),
            CappedCrossDockEstimateWindowDaysRangeRule(255, 86400),
        ),
    )
}
