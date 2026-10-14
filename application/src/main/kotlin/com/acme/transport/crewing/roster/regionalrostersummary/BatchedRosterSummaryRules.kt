package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.text.normalize

/** A rule a batched roster summary must pass. Check returns its problems, or none. */
interface BatchedRosterSummaryRule {
    fun check(item: BatchedRosterSummary): MutableList<String>
}

data class BatchedRosterSummaryRequiredCodeRule(var message: String) : BatchedRosterSummaryRule {
    override fun check(item: BatchedRosterSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterSummaryOdometerRangeRule(var min: Int, var max: Int) : BatchedRosterSummaryRule {
    override fun check(item: BatchedRosterSummary): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterSummaryAllowedCurrencyRule(
    var allowed: MutableList<String>,
) : BatchedRosterSummaryRule {
    override fun check(item: BatchedRosterSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.currency))) {
            return mutableListOf<String>("currency ${item.currency} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterSummaryLoadKgRangeRule(var min: Double, var max: Double) : BatchedRosterSummaryRule {
    override fun check(item: BatchedRosterSummary): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched roster summary. */
data class BatchedRosterSummaryValidator(var rules: MutableList<BatchedRosterSummaryRule>) {
    fun validate(item: BatchedRosterSummary): MutableList<String> {
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

    fun requireValid(item: BatchedRosterSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRosterSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRosterSummaryValidator(): BatchedRosterSummaryValidator {
    return BatchedRosterSummaryValidator(
        mutableListOf<BatchedRosterSummaryRule>(
            BatchedRosterSummaryRequiredCodeRule("batched roster summary needs this field"),
            BatchedRosterSummaryOdometerRangeRule(120, 86400),
            BatchedRosterSummaryAllowedCurrencyRule(mutableListOf<String>("legacy", "pending")),
            BatchedRosterSummaryLoadKgRangeRule(5.4, 3275.0),
        ),
    )
}
