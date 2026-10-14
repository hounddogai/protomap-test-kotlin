package com.acme.transport.linehaul.tender.rollup

/** A rule a tender metric must pass. Check returns its problems, or none. */
interface TenderMetricRule {
    fun check(item: TenderMetric): MutableList<String>
}

data class TenderMetricCodePrefixRule(var prefix: String) : TenderMetricRule {
    override fun check(item: TenderMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TenderMetricLabelLengthRule(var maxLength: Int) : TenderMetricRule {
    override fun check(item: TenderMetric): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderMetricRequiredCodeRule(var message: String) : TenderMetricRule {
    override fun check(item: TenderMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderMetricActiveGuardRule(var limit: Int) : TenderMetricRule {
    override fun check(item: TenderMetric): MutableList<String> {
        if (item.active && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class TenderMetricDepotLengthRule(var maxLength: Int) : TenderMetricRule {
    override fun check(item: TenderMetric): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderMetricReviewerLengthRule(var maxLength: Int) : TenderMetricRule {
    override fun check(item: TenderMetric): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender metric. */
data class TenderMetricValidator(var rules: MutableList<TenderMetricRule>) {
    fun validate(item: TenderMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderMetricValidator(): TenderMetricValidator {
    return TenderMetricValidator(
        mutableListOf<TenderMetricRule>(
            TenderMetricCodePrefixRule("OPS"),
            TenderMetricLabelLengthRule(64),
            TenderMetricRequiredCodeRule("tender metric needs an entry"),
            TenderMetricActiveGuardRule(16),
            TenderMetricDepotLengthRule(86400),
            TenderMetricReviewerLengthRule(120),
        ),
    )
}
