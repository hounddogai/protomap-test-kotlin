package com.acme.transport.customs.broker.brokerbatch

/** A rule a adaptive broker metric must pass. Check returns its problems, or none. */
interface AdaptiveBrokerMetricRule {
    fun check(item: AdaptiveBrokerMetric): MutableList<String>
}

data class AdaptiveBrokerMetricRequiredLaneRule(var message: String) : AdaptiveBrokerMetricRule {
    override fun check(item: AdaptiveBrokerMetric): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerMetricRequiredNameRule(var message: String) : AdaptiveBrokerMetricRule {
    override fun check(item: AdaptiveBrokerMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerMetricNameLengthRule(var maxLength: Int) : AdaptiveBrokerMetricRule {
    override fun check(item: AdaptiveBrokerMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerMetricCodePrefixRule(var prefix: String) : AdaptiveBrokerMetricRule {
    override fun check(item: AdaptiveBrokerMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerMetricRequiredCodeRule(var message: String) : AdaptiveBrokerMetricRule {
    override fun check(item: AdaptiveBrokerMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveBrokerMetricCodeLengthRule(var maxLength: Int) : AdaptiveBrokerMetricRule {
    override fun check(item: AdaptiveBrokerMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive broker metric. */
data class AdaptiveBrokerMetricValidator(var rules: MutableList<AdaptiveBrokerMetricRule>) {
    fun validate(item: AdaptiveBrokerMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveBrokerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveBrokerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveBrokerMetricValidator(): AdaptiveBrokerMetricValidator {
    return AdaptiveBrokerMetricValidator(
        mutableListOf<AdaptiveBrokerMetricRule>(
            AdaptiveBrokerMetricRequiredLaneRule("adaptive broker metric needs this field"),
            AdaptiveBrokerMetricRequiredNameRule("adaptive broker metric needs an entry"),
            AdaptiveBrokerMetricNameLengthRule(3600),
            AdaptiveBrokerMetricCodePrefixRule("ACM"),
            AdaptiveBrokerMetricRequiredCodeRule("adaptive broker metric needs a value"),
            AdaptiveBrokerMetricCodeLengthRule(255),
        ),
    )
}
