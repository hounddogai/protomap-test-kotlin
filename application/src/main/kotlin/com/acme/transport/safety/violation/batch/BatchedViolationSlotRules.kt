package com.acme.transport.safety.violation.batch

import com.acme.transport.kit.text.normalize

/** A rule a batched violation slot must pass. Check returns its problems, or none. */
interface BatchedViolationSlotRule {
    fun check(item: BatchedViolationSlot): MutableList<String>
}

data class BatchedViolationSlotRequiredNameRule(var message: String) : BatchedViolationSlotRule {
    override fun check(item: BatchedViolationSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationSlotRequiredCodeRule(var message: String) : BatchedViolationSlotRule {
    override fun check(item: BatchedViolationSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationSlotAllowedVehicleIdRule(
    var allowed: MutableList<String>,
) : BatchedViolationSlotRule {
    override fun check(item: BatchedViolationSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationSlotNameLengthRule(var maxLength: Int) : BatchedViolationSlotRule {
    override fun check(item: BatchedViolationSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedViolationSlotAmountRangeRule(var min: Double, var max: Double) : BatchedViolationSlotRule {
    override fun check(item: BatchedViolationSlot): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched violation slot. */
data class BatchedViolationSlotValidator(var rules: MutableList<BatchedViolationSlotRule>) {
    fun validate(item: BatchedViolationSlot): MutableList<String> {
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

    fun requireValid(item: BatchedViolationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedViolationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedViolationSlotValidator(): BatchedViolationSlotValidator {
    return BatchedViolationSlotValidator(
        mutableListOf<BatchedViolationSlotRule>(
            BatchedViolationSlotRequiredNameRule("batched violation slot needs a value"),
            BatchedViolationSlotRequiredCodeRule("batched violation slot needs an entry"),
            BatchedViolationSlotAllowedVehicleIdRule(mutableListOf<String>()),
            BatchedViolationSlotNameLengthRule(64),
            BatchedViolationSlotAmountRangeRule(7.8, 1601.7),
        ),
    )
}
