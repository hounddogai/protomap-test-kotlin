package com.acme.transport.crewing.restbreak.agreement

/** A rule a rest break estimate must pass. Check returns its problems, or none. */
interface RestBreakEstimateRule {
    fun check(item: RestBreakEstimate): MutableList<String>
}

data class RestBreakEstimateCodePrefixRule(var prefix: String) : RestBreakEstimateRule {
    override fun check(item: RestBreakEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakEstimateRequiredCodeRule(var message: String) : RestBreakEstimateRule {
    override fun check(item: RestBreakEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakEstimateDistanceKmRangeRule(var min: Double, var max: Double) : RestBreakEstimateRule {
    override fun check(item: RestBreakEstimate): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakEstimateCurrencyLengthRule(var maxLength: Int) : RestBreakEstimateRule {
    override fun check(item: RestBreakEstimate): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break estimate. */
data class RestBreakEstimateValidator(var rules: MutableList<RestBreakEstimateRule>) {
    fun validate(item: RestBreakEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakEstimateValidator(): RestBreakEstimateValidator {
    return RestBreakEstimateValidator(
        mutableListOf<RestBreakEstimateRule>(
            RestBreakEstimateCodePrefixRule("OPS"),
            RestBreakEstimateRequiredCodeRule("rest break estimate needs a value"),
            RestBreakEstimateDistanceKmRangeRule(1.2, 3591.3),
            RestBreakEstimateCurrencyLengthRule(80),
        ),
    )
}
