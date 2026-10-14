package com.acme.transport.freightpay.ratecard.preference

/** A rule a rolling rate card threshold must pass. Check returns its problems, or none. */
interface RollingRateCardThresholdRule {
    fun check(item: RollingRateCardThreshold): MutableList<String>
}

data class RollingRateCardThresholdBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : RollingRateCardThresholdRule {
    override fun check(item: RollingRateCardThreshold): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class RollingRateCardThresholdCodePrefixRule(var prefix: String) : RollingRateCardThresholdRule {
    override fun check(item: RollingRateCardThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingRateCardThresholdRequiredLabelRule(var message: String) : RollingRateCardThresholdRule {
    override fun check(item: RollingRateCardThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling rate card threshold. */
data class RollingRateCardThresholdValidator(var rules: MutableList<RollingRateCardThresholdRule>) {
    fun validate(item: RollingRateCardThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingRateCardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRateCardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRateCardThresholdValidator(): RollingRateCardThresholdValidator {
    return RollingRateCardThresholdValidator(
        mutableListOf<RollingRateCardThresholdRule>(
            RollingRateCardThresholdBatchSizeRangeRule(80, 255),
            RollingRateCardThresholdCodePrefixRule("OPS"),
            RollingRateCardThresholdRequiredLabelRule("rolling rate card threshold needs this field"),
        ),
    )
}
