package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize

/** A rule a weighted inspection allocation must pass. Check returns its problems, or none. */
interface WeightedInspectionAllocationRule {
    fun check(item: WeightedInspectionAllocation): MutableList<String>
}

data class WeightedInspectionAllocationCodeLengthRule(var maxLength: Int) : WeightedInspectionAllocationRule {
    override fun check(item: WeightedInspectionAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionAllocationNameLengthRule(var maxLength: Int) : WeightedInspectionAllocationRule {
    override fun check(item: WeightedInspectionAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedInspectionAllocationFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : WeightedInspectionAllocationRule {
    override fun check(item: WeightedInspectionAllocation): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class WeightedInspectionAllocationWeightRangeRule(
    var min: Double,
    var max: Double,
) : WeightedInspectionAllocationRule {
    override fun check(item: WeightedInspectionAllocation): MutableList<String> {
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

data class WeightedInspectionAllocationAllowedNameRule(
    var allowed: MutableList<String>,
) : WeightedInspectionAllocationRule {
    override fun check(item: WeightedInspectionAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted inspection allocation. */
data class WeightedInspectionAllocationValidator(var rules: MutableList<WeightedInspectionAllocationRule>) {
    fun validate(item: WeightedInspectionAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedInspectionAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedInspectionAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedInspectionAllocationValidator(): WeightedInspectionAllocationValidator {
    return WeightedInspectionAllocationValidator(
        mutableListOf<WeightedInspectionAllocationRule>(
            WeightedInspectionAllocationCodeLengthRule(86400),
            WeightedInspectionAllocationNameLengthRule(86400),
            WeightedInspectionAllocationFloorValueRangeRule(8.2, 587.0),
            WeightedInspectionAllocationWeightRangeRule(1.9, 1739.8),
            WeightedInspectionAllocationAllowedNameRule(mutableListOf<String>("global", "default", "adaptive")),
        ),
    )
}
