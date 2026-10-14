package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.text.normalize

/** A rule a pending inspection batch must pass. Check returns its problems, or none. */
interface PendingInspectionBatchRule {
    fun check(item: PendingInspectionBatch): MutableList<String>
}

data class PendingInspectionBatchAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingInspectionBatchRule {
    override fun check(item: PendingInspectionBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBatchWeightRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionBatchRule {
    override fun check(item: PendingInspectionBatch): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBatchRequiredCodeRule(var message: String) : PendingInspectionBatchRule {
    override fun check(item: PendingInspectionBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBatchRequiredTitleRule(var message: String) : PendingInspectionBatchRule {
    override fun check(item: PendingInspectionBatch): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBatchRequiredDriverIdRule(var message: String) : PendingInspectionBatchRule {
    override fun check(item: PendingInspectionBatch): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionBatchCodePrefixRule(var prefix: String) : PendingInspectionBatchRule {
    override fun check(item: PendingInspectionBatch): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection batch. */
data class PendingInspectionBatchValidator(var rules: MutableList<PendingInspectionBatchRule>) {
    fun validate(item: PendingInspectionBatch): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingInspectionBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionBatchValidator(): PendingInspectionBatchValidator {
    return PendingInspectionBatchValidator(
        mutableListOf<PendingInspectionBatchRule>(
            PendingInspectionBatchAllowedCodeRule(mutableListOf<String>()),
            PendingInspectionBatchWeightRangeRule(6.2, 2929.6),
            PendingInspectionBatchRequiredCodeRule("pending inspection batch needs a value"),
            PendingInspectionBatchRequiredTitleRule("pending inspection batch needs an entry"),
            PendingInspectionBatchRequiredDriverIdRule("pending inspection batch needs an entry"),
            PendingInspectionBatchCodePrefixRule("ACM"),
        ),
    )
}
