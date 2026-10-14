package com.acme.transport.linehaul.backhaul.allocation

/** A rule a backhaul allocation must pass. Check returns its problems, or none. */
interface BackhaulAllocationRule {
    fun check(item: BackhaulAllocation): MutableList<String>
}

data class BackhaulAllocationLabelLengthRule(var maxLength: Int) : BackhaulAllocationRule {
    override fun check(item: BackhaulAllocation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAllocationCodePrefixRule(var prefix: String) : BackhaulAllocationRule {
    override fun check(item: BackhaulAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulAllocationLimitRangeRule(var min: Int, var max: Int) : BackhaulAllocationRule {
    override fun check(item: BackhaulAllocation): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul allocation. */
data class BackhaulAllocationValidator(var rules: MutableList<BackhaulAllocationRule>) {
    fun validate(item: BackhaulAllocation): MutableList<String> {
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

    fun requireValid(item: BackhaulAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulAllocationValidator(): BackhaulAllocationValidator {
    return BackhaulAllocationValidator(
        mutableListOf<BackhaulAllocationRule>(
            BackhaulAllocationLabelLengthRule(1000),
            BackhaulAllocationCodePrefixRule("REF"),
            BackhaulAllocationLimitRangeRule(64, 120),
        ),
    )
}
