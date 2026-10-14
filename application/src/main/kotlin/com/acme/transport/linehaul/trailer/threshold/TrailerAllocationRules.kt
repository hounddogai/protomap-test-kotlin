package com.acme.transport.linehaul.trailer.threshold

/** A rule a trailer allocation must pass. Check returns its problems, or none. */
interface TrailerAllocationRule {
    fun check(item: TrailerAllocation): MutableList<String>
}

data class TrailerAllocationEndsAtRangeRule(var min: Int, var max: Int) : TrailerAllocationRule {
    override fun check(item: TrailerAllocation): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerAllocationBatchSizeRangeRule(var min: Int, var max: Int) : TrailerAllocationRule {
    override fun check(item: TrailerAllocation): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class TrailerAllocationRequiredOwnerIdRule(var message: String) : TrailerAllocationRule {
    override fun check(item: TrailerAllocation): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer allocation. */
data class TrailerAllocationValidator(var rules: MutableList<TrailerAllocationRule>) {
    fun validate(item: TrailerAllocation): MutableList<String> {
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

    fun requireValid(item: TrailerAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerAllocationValidator(): TrailerAllocationValidator {
    return TrailerAllocationValidator(
        mutableListOf<TrailerAllocationRule>(
            TrailerAllocationEndsAtRangeRule(16, 64),
            TrailerAllocationBatchSizeRangeRule(32, 86400),
            TrailerAllocationRequiredOwnerIdRule("trailer allocation needs a value"),
        ),
    )
}
