package com.acme.transport.safety.violation.snapshot

/** A rule a global violation allocation must pass. Check returns its problems, or none. */
interface GlobalViolationAllocationRule {
    fun check(item: GlobalViolationAllocation): MutableList<String>
}

data class GlobalViolationAllocationBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : GlobalViolationAllocationRule {
    override fun check(item: GlobalViolationAllocation): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationAllocationRequiredTitleRule(var message: String) : GlobalViolationAllocationRule {
    override fun check(item: GlobalViolationAllocation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalViolationAllocationWeightRangeRule(
    var min: Double,
    var max: Double,
) : GlobalViolationAllocationRule {
    override fun check(item: GlobalViolationAllocation): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a global violation allocation. */
data class GlobalViolationAllocationValidator(var rules: MutableList<GlobalViolationAllocationRule>) {
    fun validate(item: GlobalViolationAllocation): MutableList<String> {
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

    fun requireValid(item: GlobalViolationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalViolationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalViolationAllocationValidator(): GlobalViolationAllocationValidator {
    return GlobalViolationAllocationValidator(
        mutableListOf<GlobalViolationAllocationRule>(
            GlobalViolationAllocationBatchSizeRangeRule(32, 3600),
            GlobalViolationAllocationRequiredTitleRule("global violation allocation needs this field"),
            GlobalViolationAllocationWeightRangeRule(1.9, 2091.8),
        ),
    )
}
