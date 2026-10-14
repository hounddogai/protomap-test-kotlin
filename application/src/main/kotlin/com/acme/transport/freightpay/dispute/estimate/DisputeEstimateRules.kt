package com.acme.transport.freightpay.dispute.estimate

/** A rule a dispute estimate must pass. Check returns its problems, or none. */
interface DisputeEstimateRule {
    fun check(item: DisputeEstimate): MutableList<String>
}

data class DisputeEstimateBatchSizeRangeRule(var min: Int, var max: Int) : DisputeEstimateRule {
    override fun check(item: DisputeEstimate): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeEstimateRequiredCodeRule(var message: String) : DisputeEstimateRule {
    override fun check(item: DisputeEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeEstimateRequiredLabelRule(var message: String) : DisputeEstimateRule {
    override fun check(item: DisputeEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeEstimateMinimumRangeRule(var min: Double, var max: Double) : DisputeEstimateRule {
    override fun check(item: DisputeEstimate): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DisputeEstimateRegionLengthRule(var maxLength: Int) : DisputeEstimateRule {
    override fun check(item: DisputeEstimate): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute estimate. */
data class DisputeEstimateValidator(var rules: MutableList<DisputeEstimateRule>) {
    fun validate(item: DisputeEstimate): MutableList<String> {
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

    fun requireValid(item: DisputeEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeEstimateValidator(): DisputeEstimateValidator {
    return DisputeEstimateValidator(
        mutableListOf<DisputeEstimateRule>(
            DisputeEstimateBatchSizeRangeRule(120, 86400),
            DisputeEstimateRequiredCodeRule("dispute estimate needs a value"),
            DisputeEstimateRequiredLabelRule("dispute estimate needs this field"),
            DisputeEstimateMinimumRangeRule(2.1, 1722.1),
            DisputeEstimateRegionLengthRule(86400),
        ),
    )
}
