package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.text.normalize

/** A rule a batched duty period forecast must pass. Check returns its problems, or none. */
interface BatchedDutyPeriodForecastRule {
    fun check(item: BatchedDutyPeriodForecast): MutableList<String>
}

data class BatchedDutyPeriodForecastRequiredNameRule(var message: String) : BatchedDutyPeriodForecastRule {
    override fun check(item: BatchedDutyPeriodForecast): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodForecastNameLengthRule(var maxLength: Int) : BatchedDutyPeriodForecastRule {
    override fun check(item: BatchedDutyPeriodForecast): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodForecastReviewerLengthRule(var maxLength: Int) : BatchedDutyPeriodForecastRule {
    override fun check(item: BatchedDutyPeriodForecast): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodForecastAllowedCodeRule(
    var allowed: MutableList<String>,
) : BatchedDutyPeriodForecastRule {
    override fun check(item: BatchedDutyPeriodForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodForecastCodePrefixRule(var prefix: String) : BatchedDutyPeriodForecastRule {
    override fun check(item: BatchedDutyPeriodForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDutyPeriodForecastRequiredCodeRule(var message: String) : BatchedDutyPeriodForecastRule {
    override fun check(item: BatchedDutyPeriodForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched duty period forecast. */
data class BatchedDutyPeriodForecastValidator(var rules: MutableList<BatchedDutyPeriodForecastRule>) {
    fun validate(item: BatchedDutyPeriodForecast): MutableList<String> {
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

    fun requireValid(item: BatchedDutyPeriodForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDutyPeriodForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDutyPeriodForecastValidator(): BatchedDutyPeriodForecastValidator {
    return BatchedDutyPeriodForecastValidator(
        mutableListOf<BatchedDutyPeriodForecastRule>(
            BatchedDutyPeriodForecastRequiredNameRule("batched duty period forecast needs this field"),
            BatchedDutyPeriodForecastNameLengthRule(80),
            BatchedDutyPeriodForecastReviewerLengthRule(80),
            BatchedDutyPeriodForecastAllowedCodeRule(mutableListOf<String>("seasonal", "rolling", "tiered")),
            BatchedDutyPeriodForecastCodePrefixRule("REF"),
            BatchedDutyPeriodForecastRequiredCodeRule("batched duty period forecast needs an entry"),
        ),
    )
}
