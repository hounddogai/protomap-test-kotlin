package com.acme.transport.customs.broker.slot

/** A rule a pending broker allocation must pass. Check returns its problems, or none. */
interface PendingBrokerAllocationRule {
    fun check(item: PendingBrokerAllocation): MutableList<String>
}

data class PendingBrokerAllocationRequiredCodeRule(var message: String) : PendingBrokerAllocationRule {
    override fun check(item: PendingBrokerAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerAllocationDiscountRangeRule(
    var min: Double,
    var max: Double,
) : PendingBrokerAllocationRule {
    override fun check(item: PendingBrokerAllocation): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerAllocationActiveGuardRule(var limit: Double) : PendingBrokerAllocationRule {
    override fun check(item: PendingBrokerAllocation): MutableList<String> {
        if (item.active && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingBrokerAllocationUnitLengthRule(var maxLength: Int) : PendingBrokerAllocationRule {
    override fun check(item: PendingBrokerAllocation): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending broker allocation. */
data class PendingBrokerAllocationValidator(var rules: MutableList<PendingBrokerAllocationRule>) {
    fun validate(item: PendingBrokerAllocation): MutableList<String> {
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

    fun requireValid(item: PendingBrokerAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBrokerAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBrokerAllocationValidator(): PendingBrokerAllocationValidator {
    return PendingBrokerAllocationValidator(
        mutableListOf<PendingBrokerAllocationRule>(
            PendingBrokerAllocationRequiredCodeRule("pending broker allocation needs a value"),
            PendingBrokerAllocationDiscountRangeRule(8.3, 790.8),
            PendingBrokerAllocationActiveGuardRule(1.4),
            PendingBrokerAllocationUnitLengthRule(255),
        ),
    )
}
