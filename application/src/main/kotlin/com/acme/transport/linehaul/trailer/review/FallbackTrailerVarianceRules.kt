package com.acme.transport.linehaul.trailer.review

/** A rule a fallback trailer variance must pass. Check returns its problems, or none. */
interface FallbackTrailerVarianceRule {
    fun check(item: FallbackTrailerVariance): MutableList<String>
}

data class FallbackTrailerVarianceNameLengthRule(var maxLength: Int) : FallbackTrailerVarianceRule {
    override fun check(item: FallbackTrailerVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerVarianceCategoryLengthRule(var maxLength: Int) : FallbackTrailerVarianceRule {
    override fun check(item: FallbackTrailerVariance): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerVarianceRequiredNameRule(var message: String) : FallbackTrailerVarianceRule {
    override fun check(item: FallbackTrailerVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerVarianceRequiredCategoryRule(var message: String) : FallbackTrailerVarianceRule {
    override fun check(item: FallbackTrailerVariance): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackTrailerVarianceLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackTrailerVarianceRule {
    override fun check(item: FallbackTrailerVariance): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class FallbackTrailerVarianceMaximumRangeRule(
    var min: Double,
    var max: Double,
) : FallbackTrailerVarianceRule {
    override fun check(item: FallbackTrailerVariance): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback trailer variance. */
data class FallbackTrailerVarianceValidator(var rules: MutableList<FallbackTrailerVarianceRule>) {
    fun validate(item: FallbackTrailerVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackTrailerVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTrailerVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTrailerVarianceValidator(): FallbackTrailerVarianceValidator {
    return FallbackTrailerVarianceValidator(
        mutableListOf<FallbackTrailerVarianceRule>(
            FallbackTrailerVarianceNameLengthRule(16),
            FallbackTrailerVarianceCategoryLengthRule(120),
            FallbackTrailerVarianceRequiredNameRule("fallback trailer variance needs a value"),
            FallbackTrailerVarianceRequiredCategoryRule("fallback trailer variance needs this field"),
            FallbackTrailerVarianceLoadKgRangeRule(1.7, 391.8),
            FallbackTrailerVarianceMaximumRangeRule(8.3, 1392.7),
        ),
    )
}
