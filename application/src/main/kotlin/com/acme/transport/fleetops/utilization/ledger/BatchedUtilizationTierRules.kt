package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.text.normalize

/** A rule a batched utilization tier must pass. Check returns its problems, or none. */
interface BatchedUtilizationTierRule {
    fun check(item: BatchedUtilizationTier): MutableList<String>
}

data class BatchedUtilizationTierAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : BatchedUtilizationTierRule {
    override fun check(item: BatchedUtilizationTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationTierLabelLengthRule(var maxLength: Int) : BatchedUtilizationTierRule {
    override fun check(item: BatchedUtilizationTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedUtilizationTierRequiredLabelRule(var message: String) : BatchedUtilizationTierRule {
    override fun check(item: BatchedUtilizationTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched utilization tier. */
data class BatchedUtilizationTierValidator(var rules: MutableList<BatchedUtilizationTierRule>) {
    fun validate(item: BatchedUtilizationTier): MutableList<String> {
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

    fun requireValid(item: BatchedUtilizationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedUtilizationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedUtilizationTierValidator(): BatchedUtilizationTierValidator {
    return BatchedUtilizationTierValidator(
        mutableListOf<BatchedUtilizationTierRule>(
            BatchedUtilizationTierAllowedShiftCodeRule(mutableListOf<String>()),
            BatchedUtilizationTierLabelLengthRule(255),
            BatchedUtilizationTierRequiredLabelRule("batched utilization tier needs a value"),
        ),
    )
}
