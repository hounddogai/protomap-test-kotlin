package com.acme.transport.freightpay.dispute.metric

/** A rule a lenient dispute metric must pass. Check returns its problems, or none. */
interface LenientDisputeMetricRule {
    fun check(item: LenientDisputeMetric): MutableList<String>
}

data class LenientDisputeMetricRevisionRangeRule(var min: Int, var max: Int) : LenientDisputeMetricRule {
    override fun check(item: LenientDisputeMetric): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeMetricCategoryLengthRule(var maxLength: Int) : LenientDisputeMetricRule {
    override fun check(item: LenientDisputeMetric): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeMetricCarrierLengthRule(var maxLength: Int) : LenientDisputeMetricRule {
    override fun check(item: LenientDisputeMetric): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDisputeMetricRequiredCategoryRule(var message: String) : LenientDisputeMetricRule {
    override fun check(item: LenientDisputeMetric): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dispute metric. */
data class LenientDisputeMetricValidator(var rules: MutableList<LenientDisputeMetricRule>) {
    fun validate(item: LenientDisputeMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDisputeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDisputeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDisputeMetricValidator(): LenientDisputeMetricValidator {
    return LenientDisputeMetricValidator(
        mutableListOf<LenientDisputeMetricRule>(
            LenientDisputeMetricRevisionRangeRule(64, 3600),
            LenientDisputeMetricCategoryLengthRule(64),
            LenientDisputeMetricCarrierLengthRule(16),
            LenientDisputeMetricRequiredCategoryRule("lenient dispute metric needs an entry"),
        ),
    )
}
