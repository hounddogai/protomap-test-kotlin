package com.acme.transport.customs.hold.holdallocation

/** A rule a scheduled hold allocation must pass. Check returns its problems, or none. */
interface ScheduledHoldAllocationRule {
    fun check(item: ScheduledHoldAllocation): MutableList<String>
}

data class ScheduledHoldAllocationRequiredLabelRule(var message: String) : ScheduledHoldAllocationRule {
    override fun check(item: ScheduledHoldAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldAllocationCodeLengthRule(var maxLength: Int) : ScheduledHoldAllocationRule {
    override fun check(item: ScheduledHoldAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldAllocationRequiredReviewerRule(var message: String) : ScheduledHoldAllocationRule {
    override fun check(item: ScheduledHoldAllocation): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldAllocationQuantityRangeRule(var min: Int, var max: Int) : ScheduledHoldAllocationRule {
    override fun check(item: ScheduledHoldAllocation): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldAllocationRequiredCodeRule(var message: String) : ScheduledHoldAllocationRule {
    override fun check(item: ScheduledHoldAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled hold allocation. */
data class ScheduledHoldAllocationValidator(var rules: MutableList<ScheduledHoldAllocationRule>) {
    fun validate(item: ScheduledHoldAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHoldAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldAllocationValidator(): ScheduledHoldAllocationValidator {
    return ScheduledHoldAllocationValidator(
        mutableListOf<ScheduledHoldAllocationRule>(
            ScheduledHoldAllocationRequiredLabelRule("scheduled hold allocation needs this field"),
            ScheduledHoldAllocationCodeLengthRule(1000),
            ScheduledHoldAllocationRequiredReviewerRule("scheduled hold allocation needs this field"),
            ScheduledHoldAllocationQuantityRangeRule(16, 120),
            ScheduledHoldAllocationRequiredCodeRule("scheduled hold allocation needs an entry"),
        ),
    )
}
