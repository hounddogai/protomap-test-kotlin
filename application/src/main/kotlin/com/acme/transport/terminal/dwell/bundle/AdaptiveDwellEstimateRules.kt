package com.acme.transport.terminal.dwell.bundle

/** A rule a adaptive dwell estimate must pass. Check returns its problems, or none. */
interface AdaptiveDwellEstimateRule {
    fun check(item: AdaptiveDwellEstimate): MutableList<String>
}

data class AdaptiveDwellEstimateOdometerRangeRule(var min: Int, var max: Int) : AdaptiveDwellEstimateRule {
    override fun check(item: AdaptiveDwellEstimate): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class AdaptiveDwellEstimateRequiredCodeRule(var message: String) : AdaptiveDwellEstimateRule {
    override fun check(item: AdaptiveDwellEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDwellEstimateCodePrefixRule(var prefix: String) : AdaptiveDwellEstimateRule {
    override fun check(item: AdaptiveDwellEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveDwellEstimateLabelLengthRule(var maxLength: Int) : AdaptiveDwellEstimateRule {
    override fun check(item: AdaptiveDwellEstimate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive dwell estimate. */
data class AdaptiveDwellEstimateValidator(var rules: MutableList<AdaptiveDwellEstimateRule>) {
    fun validate(item: AdaptiveDwellEstimate): MutableList<String> {
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

    fun requireValid(item: AdaptiveDwellEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveDwellEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveDwellEstimateValidator(): AdaptiveDwellEstimateValidator {
    return AdaptiveDwellEstimateValidator(
        mutableListOf<AdaptiveDwellEstimateRule>(
            AdaptiveDwellEstimateOdometerRangeRule(1000, 3600),
            AdaptiveDwellEstimateRequiredCodeRule("adaptive dwell estimate needs this field"),
            AdaptiveDwellEstimateCodePrefixRule("Q"),
            AdaptiveDwellEstimateLabelLengthRule(86400),
        ),
    )
}
