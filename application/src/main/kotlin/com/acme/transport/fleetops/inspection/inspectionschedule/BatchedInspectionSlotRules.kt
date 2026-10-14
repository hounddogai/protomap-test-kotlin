package com.acme.transport.fleetops.inspection.inspectionschedule

/** A rule a batched inspection slot must pass. Check returns its problems, or none. */
interface BatchedInspectionSlotRule {
    fun check(item: BatchedInspectionSlot): MutableList<String>
}

data class BatchedInspectionSlotDiscountRangeRule(
    var min: Double,
    var max: Double,
) : BatchedInspectionSlotRule {
    override fun check(item: BatchedInspectionSlot): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionSlotTitleLengthRule(var maxLength: Int) : BatchedInspectionSlotRule {
    override fun check(item: BatchedInspectionSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionSlotCodePrefixRule(var prefix: String) : BatchedInspectionSlotRule {
    override fun check(item: BatchedInspectionSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionSlotRequiredShiftCodeRule(var message: String) : BatchedInspectionSlotRule {
    override fun check(item: BatchedInspectionSlot): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched inspection slot. */
data class BatchedInspectionSlotValidator(var rules: MutableList<BatchedInspectionSlotRule>) {
    fun validate(item: BatchedInspectionSlot): MutableList<String> {
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

    fun requireValid(item: BatchedInspectionSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedInspectionSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedInspectionSlotValidator(): BatchedInspectionSlotValidator {
    return BatchedInspectionSlotValidator(
        mutableListOf<BatchedInspectionSlotRule>(
            BatchedInspectionSlotDiscountRangeRule(7.2, 143.2),
            BatchedInspectionSlotTitleLengthRule(64),
            BatchedInspectionSlotCodePrefixRule("X"),
            BatchedInspectionSlotRequiredShiftCodeRule("batched inspection slot needs a value"),
        ),
    )
}
