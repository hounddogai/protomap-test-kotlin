package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.normalize

/** A rule a pending tender metric must pass. Check returns its problems, or none. */
interface PendingTenderMetricRule {
    fun check(item: PendingTenderMetric): MutableList<String>
}

data class PendingTenderMetricFuelLitersRangeRule(var min: Double, var max: Double) : PendingTenderMetricRule {
    override fun check(item: PendingTenderMetric): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderMetricCeilingRangeRule(var min: Double, var max: Double) : PendingTenderMetricRule {
    override fun check(item: PendingTenderMetric): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderMetricRequiredNameRule(var message: String) : PendingTenderMetricRule {
    override fun check(item: PendingTenderMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderMetricAllowedSummaryRule(var allowed: MutableList<String>) : PendingTenderMetricRule {
    override fun check(item: PendingTenderMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PendingTenderMetricNameLengthRule(var maxLength: Int) : PendingTenderMetricRule {
    override fun check(item: PendingTenderMetric): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tender metric. */
data class PendingTenderMetricValidator(var rules: MutableList<PendingTenderMetricRule>) {
    fun validate(item: PendingTenderMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTenderMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTenderMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTenderMetricValidator(): PendingTenderMetricValidator {
    return PendingTenderMetricValidator(
        mutableListOf<PendingTenderMetricRule>(
            PendingTenderMetricFuelLitersRangeRule(3.1, 1787.6),
            PendingTenderMetricCeilingRangeRule(6.0, 3723.5),
            PendingTenderMetricRequiredNameRule("pending tender metric needs this field"),
            PendingTenderMetricAllowedSummaryRule(mutableListOf<String>("seasonal", "capped")),
            PendingTenderMetricNameLengthRule(120),
        ),
    )
}
