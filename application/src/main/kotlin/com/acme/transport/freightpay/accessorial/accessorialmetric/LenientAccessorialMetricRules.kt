package com.acme.transport.freightpay.accessorial.accessorialmetric

/** A rule a lenient accessorial metric must pass. Check returns its problems, or none. */
interface LenientAccessorialMetricRule {
    fun check(item: LenientAccessorialMetric): MutableList<String>
}

data class LenientAccessorialMetricRequiredCodeRule(var message: String) : LenientAccessorialMetricRule {
    override fun check(item: LenientAccessorialMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialMetricStopCountRangeRule(
    var min: Int,
    var max: Int,
) : LenientAccessorialMetricRule {
    override fun check(item: LenientAccessorialMetric): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialMetricDescriptionLengthRule(var maxLength: Int) : LenientAccessorialMetricRule {
    override fun check(item: LenientAccessorialMetric): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialMetricRequiredDescriptionRule(var message: String) : LenientAccessorialMetricRule {
    override fun check(item: LenientAccessorialMetric): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAccessorialMetricLabelLengthRule(var maxLength: Int) : LenientAccessorialMetricRule {
    override fun check(item: LenientAccessorialMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient accessorial metric. */
data class LenientAccessorialMetricValidator(var rules: MutableList<LenientAccessorialMetricRule>) {
    fun validate(item: LenientAccessorialMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientAccessorialMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAccessorialMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAccessorialMetricValidator(): LenientAccessorialMetricValidator {
    return LenientAccessorialMetricValidator(
        mutableListOf<LenientAccessorialMetricRule>(
            LenientAccessorialMetricRequiredCodeRule("lenient accessorial metric needs this field"),
            LenientAccessorialMetricStopCountRangeRule(32, 86400),
            LenientAccessorialMetricDescriptionLengthRule(32),
            LenientAccessorialMetricRequiredDescriptionRule("lenient accessorial metric needs a value"),
            LenientAccessorialMetricLabelLengthRule(80),
        ),
    )
}
