package com.acme.transport.customs.inspection.allocation

/** A rule a inspection allocation must pass. Check returns its problems, or none. */
interface InspectionAllocationRule {
    fun check(item: InspectionAllocation): MutableList<String>
}

data class InspectionAllocationNameLengthRule(var maxLength: Int) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAllocationDockLengthRule(var maxLength: Int) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAllocationRatioRangeRule(var min: Double, var max: Double) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class InspectionAllocationPriorityRangeRule(var min: Int, var max: Int) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAllocationCodePrefixRule(var prefix: String) : InspectionAllocationRule {
    override fun check(item: InspectionAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection allocation. */
data class InspectionAllocationValidator(var rules: MutableList<InspectionAllocationRule>) {
    fun validate(item: InspectionAllocation): MutableList<String> {
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

    fun requireValid(item: InspectionAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAllocationValidator(): InspectionAllocationValidator {
    return InspectionAllocationValidator(
        mutableListOf<InspectionAllocationRule>(
            InspectionAllocationNameLengthRule(64),
            InspectionAllocationDockLengthRule(3600),
            InspectionAllocationRatioRangeRule(6.2, 822.5),
            InspectionAllocationPriorityRangeRule(86400, 86400),
            InspectionAllocationCodePrefixRule("ACM"),
        ),
    )
}
