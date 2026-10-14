package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a inspection budget must pass. Check returns its problems, or none. */
interface InspectionBudgetRule {
    fun check(item: InspectionBudget): MutableList<String>
}

data class InspectionBudgetFloorValueRangeRule(var min: Double, var max: Double) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetAllowedTitleRule(var allowed: MutableList<String>) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetCodeLengthRule(var maxLength: Int) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBudgetOwnerIdLengthRule(var maxLength: Int) : InspectionBudgetRule {
    override fun check(item: InspectionBudget): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection budget. */
data class InspectionBudgetValidator(var rules: MutableList<InspectionBudgetRule>) {
    fun validate(item: InspectionBudget): MutableList<String> {
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

    fun requireValid(item: InspectionBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionBudgetValidator(): InspectionBudgetValidator {
    return InspectionBudgetValidator(
        mutableListOf<InspectionBudgetRule>(
            InspectionBudgetFloorValueRangeRule(7.5, 3997.5),
            InspectionBudgetAllowedTitleRule(mutableListOf<String>("scheduled", "legacy")),
            InspectionBudgetCodeLengthRule(80),
            InspectionBudgetOwnerIdLengthRule(255),
        ),
    )
}
