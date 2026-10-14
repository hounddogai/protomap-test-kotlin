package com.acme.transport.linehaul.trailer.rule

/** A rule a primary trailer bundle must pass. Check returns its problems, or none. */
interface PrimaryTrailerBundleRule {
    fun check(item: PrimaryTrailerBundle): MutableList<String>
}

data class PrimaryTrailerBundleRequiredLocaleRule(var message: String) : PrimaryTrailerBundleRule {
    override fun check(item: PrimaryTrailerBundle): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerBundleLocaleLengthRule(var maxLength: Int) : PrimaryTrailerBundleRule {
    override fun check(item: PrimaryTrailerBundle): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryTrailerBundleCostRangeRule(var min: Double, var max: Double) : PrimaryTrailerBundleRule {
    override fun check(item: PrimaryTrailerBundle): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a primary trailer bundle. */
data class PrimaryTrailerBundleValidator(var rules: MutableList<PrimaryTrailerBundleRule>) {
    fun validate(item: PrimaryTrailerBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryTrailerBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryTrailerBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryTrailerBundleValidator(): PrimaryTrailerBundleValidator {
    return PrimaryTrailerBundleValidator(
        mutableListOf<PrimaryTrailerBundleRule>(
            PrimaryTrailerBundleRequiredLocaleRule("primary trailer bundle needs a value"),
            PrimaryTrailerBundleLocaleLengthRule(120),
            PrimaryTrailerBundleCostRangeRule(8.8, 3481.9),
        ),
    )
}
