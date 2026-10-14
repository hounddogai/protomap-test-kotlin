package com.acme.transport.terminal.sortation.sortationadjustment

/** A rule a adaptive sortation batch must pass. Check returns its problems, or none. */
interface AdaptiveSortationBatchRule {
    fun check(item: AdaptiveSortationBatch): MutableList<String>
}

data class AdaptiveSortationBatchRequiredLabelRule(var message: String) : AdaptiveSortationBatchRule {
    override fun check(item: AdaptiveSortationBatch): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationBatchCodePrefixRule(var prefix: String) : AdaptiveSortationBatchRule {
    override fun check(item: AdaptiveSortationBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveSortationBatchThresholdRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveSortationBatchRule {
    override fun check(item: AdaptiveSortationBatch): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a adaptive sortation batch. */
data class AdaptiveSortationBatchValidator(var rules: MutableList<AdaptiveSortationBatchRule>) {
    fun validate(item: AdaptiveSortationBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveSortationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveSortationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveSortationBatchValidator(): AdaptiveSortationBatchValidator {
    return AdaptiveSortationBatchValidator(
        mutableListOf<AdaptiveSortationBatchRule>(
            AdaptiveSortationBatchRequiredLabelRule("adaptive sortation batch needs this field"),
            AdaptiveSortationBatchCodePrefixRule("Q"),
            AdaptiveSortationBatchThresholdRangeRule(5.4, 3374.4),
        ),
    )
}
