package com.acme.transport.lastmile.redelivery.allocation

/** A rule a pending redelivery allocation must pass. Check returns its problems, or none. */
interface PendingRedeliveryAllocationRule {
    fun check(item: PendingRedeliveryAllocation): MutableList<String>
}

data class PendingRedeliveryAllocationQuantityRangeRule(
    var min: Int,
    var max: Int,
) : PendingRedeliveryAllocationRule {
    override fun check(item: PendingRedeliveryAllocation): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRedeliveryAllocationDescriptionLengthRule(
    var maxLength: Int,
) : PendingRedeliveryAllocationRule {
    override fun check(item: PendingRedeliveryAllocation): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRedeliveryAllocationGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : PendingRedeliveryAllocationRule {
    override fun check(item: PendingRedeliveryAllocation): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingRedeliveryAllocationRequiredCodeRule(var message: String) : PendingRedeliveryAllocationRule {
    override fun check(item: PendingRedeliveryAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRedeliveryAllocationRequiredDescriptionRule(
    var message: String,
) : PendingRedeliveryAllocationRule {
    override fun check(item: PendingRedeliveryAllocation): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending redelivery allocation. */
data class PendingRedeliveryAllocationValidator(var rules: MutableList<PendingRedeliveryAllocationRule>) {
    fun validate(item: PendingRedeliveryAllocation): MutableList<String> {
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

    fun requireValid(item: PendingRedeliveryAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRedeliveryAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRedeliveryAllocationValidator(): PendingRedeliveryAllocationValidator {
    return PendingRedeliveryAllocationValidator(
        mutableListOf<PendingRedeliveryAllocationRule>(
            PendingRedeliveryAllocationQuantityRangeRule(32, 32),
            PendingRedeliveryAllocationDescriptionLengthRule(1000),
            PendingRedeliveryAllocationGraceDaysRangeRule(64, 3600),
            PendingRedeliveryAllocationRequiredCodeRule("pending redelivery allocation needs a value"),
            PendingRedeliveryAllocationRequiredDescriptionRule("pending redelivery allocation needs this field"),
        ),
    )
}
