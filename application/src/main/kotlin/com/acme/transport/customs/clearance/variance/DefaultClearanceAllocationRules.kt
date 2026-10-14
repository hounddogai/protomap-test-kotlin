package com.acme.transport.customs.clearance.variance

/** A rule a default clearance allocation must pass. Check returns its problems, or none. */
interface DefaultClearanceAllocationRule {
    fun check(item: DefaultClearanceAllocation): MutableList<String>
}

data class DefaultClearanceAllocationRequiredCodeRule(var message: String) : DefaultClearanceAllocationRule {
    override fun check(item: DefaultClearanceAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceAllocationFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : DefaultClearanceAllocationRule {
    override fun check(item: DefaultClearanceAllocation): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceAllocationCodePrefixRule(var prefix: String) : DefaultClearanceAllocationRule {
    override fun check(item: DefaultClearanceAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceAllocationAmountRangeRule(
    var min: Double,
    var max: Double,
) : DefaultClearanceAllocationRule {
    override fun check(item: DefaultClearanceAllocation): MutableList<String> {
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

data class DefaultClearanceAllocationRequiredExternalRefRule(
    var message: String,
) : DefaultClearanceAllocationRule {
    override fun check(item: DefaultClearanceAllocation): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultClearanceAllocationCodeLengthRule(var maxLength: Int) : DefaultClearanceAllocationRule {
    override fun check(item: DefaultClearanceAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default clearance allocation. */
data class DefaultClearanceAllocationValidator(var rules: MutableList<DefaultClearanceAllocationRule>) {
    fun validate(item: DefaultClearanceAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultClearanceAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultClearanceAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultClearanceAllocationValidator(): DefaultClearanceAllocationValidator {
    return DefaultClearanceAllocationValidator(
        mutableListOf<DefaultClearanceAllocationRule>(
            DefaultClearanceAllocationRequiredCodeRule("default clearance allocation needs this field"),
            DefaultClearanceAllocationFloorValueRangeRule(2.6, 3482.6),
            DefaultClearanceAllocationCodePrefixRule("X"),
            DefaultClearanceAllocationAmountRangeRule(1.2, 4618.5),
            DefaultClearanceAllocationRequiredExternalRefRule("default clearance allocation needs this field"),
            DefaultClearanceAllocationCodeLengthRule(3600),
        ),
    )
}
