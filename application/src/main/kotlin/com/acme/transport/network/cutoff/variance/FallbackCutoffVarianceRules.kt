package com.acme.transport.network.cutoff.variance

/** A rule a fallback cutoff variance must pass. Check returns its problems, or none. */
interface FallbackCutoffVarianceRule {
    fun check(item: FallbackCutoffVariance): MutableList<String>
}

data class FallbackCutoffVarianceNameLengthRule(var maxLength: Int) : FallbackCutoffVarianceRule {
    override fun check(item: FallbackCutoffVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffVarianceCreatedAtRangeRule(var min: Int, var max: Int) : FallbackCutoffVarianceRule {
    override fun check(item: FallbackCutoffVariance): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCutoffVarianceRequiredCodeRule(var message: String) : FallbackCutoffVarianceRule {
    override fun check(item: FallbackCutoffVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback cutoff variance. */
data class FallbackCutoffVarianceValidator(var rules: MutableList<FallbackCutoffVarianceRule>) {
    fun validate(item: FallbackCutoffVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackCutoffVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCutoffVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCutoffVarianceValidator(): FallbackCutoffVarianceValidator {
    return FallbackCutoffVarianceValidator(
        mutableListOf<FallbackCutoffVarianceRule>(
            FallbackCutoffVarianceNameLengthRule(86400),
            FallbackCutoffVarianceCreatedAtRangeRule(120, 255),
            FallbackCutoffVarianceRequiredCodeRule("fallback cutoff variance needs an entry"),
        ),
    )
}
