package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize

/** A rule a inspection cycle must pass. Check returns its problems, or none. */
interface InspectionCycleRule {
    fun check(item: InspectionCycle): MutableList<String>
}

data class InspectionCycleRequiredNameRule(var message: String) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionCycleCodeLengthRule(var maxLength: Int) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionCycleQuantityRangeRule(var min: Int, var max: Int) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class InspectionCycleRequiredReviewerRule(var message: String) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionCycleRequiredCodeRule(var message: String) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionCycleAllowedShiftCodeRule(var allowed: MutableList<String>) : InspectionCycleRule {
    override fun check(item: InspectionCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection cycle. */
data class InspectionCycleValidator(var rules: MutableList<InspectionCycleRule>) {
    fun validate(item: InspectionCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionCycleValidator(): InspectionCycleValidator {
    return InspectionCycleValidator(
        mutableListOf<InspectionCycleRule>(
            InspectionCycleRequiredNameRule("inspection cycle needs an entry"),
            InspectionCycleCodeLengthRule(3600),
            InspectionCycleQuantityRangeRule(16, 16),
            InspectionCycleRequiredReviewerRule("inspection cycle needs an entry"),
            InspectionCycleRequiredCodeRule("inspection cycle needs an entry"),
            InspectionCycleAllowedShiftCodeRule(mutableListOf<String>("batched", "capped")),
        ),
    )
}
