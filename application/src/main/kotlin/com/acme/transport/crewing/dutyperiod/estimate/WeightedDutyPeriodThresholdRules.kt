package com.acme.transport.crewing.dutyperiod.estimate

/** A rule a weighted duty period threshold must pass. Check returns its problems, or none. */
interface WeightedDutyPeriodThresholdRule {
    fun check(item: WeightedDutyPeriodThreshold): MutableList<String>
}

data class WeightedDutyPeriodThresholdRequiredLabelRule(var message: String) : WeightedDutyPeriodThresholdRule {
    override fun check(item: WeightedDutyPeriodThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodThresholdRequiredCategoryRule(
    var message: String,
) : WeightedDutyPeriodThresholdRule {
    override fun check(item: WeightedDutyPeriodThreshold): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodThresholdLabelLengthRule(var maxLength: Int) : WeightedDutyPeriodThresholdRule {
    override fun check(item: WeightedDutyPeriodThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodThresholdCodePrefixRule(var prefix: String) : WeightedDutyPeriodThresholdRule {
    override fun check(item: WeightedDutyPeriodThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodThresholdRequiredCodeRule(var message: String) : WeightedDutyPeriodThresholdRule {
    override fun check(item: WeightedDutyPeriodThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDutyPeriodThresholdCategoryLengthRule(var maxLength: Int) : WeightedDutyPeriodThresholdRule {
    override fun check(item: WeightedDutyPeriodThreshold): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted duty period threshold. */
data class WeightedDutyPeriodThresholdValidator(var rules: MutableList<WeightedDutyPeriodThresholdRule>) {
    fun validate(item: WeightedDutyPeriodThreshold): MutableList<String> {
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

    fun requireValid(item: WeightedDutyPeriodThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDutyPeriodThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDutyPeriodThresholdValidator(): WeightedDutyPeriodThresholdValidator {
    return WeightedDutyPeriodThresholdValidator(
        mutableListOf<WeightedDutyPeriodThresholdRule>(
            WeightedDutyPeriodThresholdRequiredLabelRule("weighted duty period threshold needs this field"),
            WeightedDutyPeriodThresholdRequiredCategoryRule("weighted duty period threshold needs a value"),
            WeightedDutyPeriodThresholdLabelLengthRule(1000),
            WeightedDutyPeriodThresholdCodePrefixRule("Q"),
            WeightedDutyPeriodThresholdRequiredCodeRule("weighted duty period threshold needs this field"),
            WeightedDutyPeriodThresholdCategoryLengthRule(3600),
        ),
    )
}
