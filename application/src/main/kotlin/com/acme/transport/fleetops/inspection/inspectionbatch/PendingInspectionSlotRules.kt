package com.acme.transport.fleetops.inspection.inspectionbatch

/** A rule a pending inspection slot must pass. Check returns its problems, or none. */
interface PendingInspectionSlotRule {
    fun check(item: PendingInspectionSlot): MutableList<String>
}

data class PendingInspectionSlotRequiredCodeRule(var message: String) : PendingInspectionSlotRule {
    override fun check(item: PendingInspectionSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionSlotLimitRangeRule(var min: Int, var max: Int) : PendingInspectionSlotRule {
    override fun check(item: PendingInspectionSlot): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionSlotCategoryLengthRule(var maxLength: Int) : PendingInspectionSlotRule {
    override fun check(item: PendingInspectionSlot): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionSlotRequiredRouteCodeRule(var message: String) : PendingInspectionSlotRule {
    override fun check(item: PendingInspectionSlot): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionSlotRouteCodeLengthRule(var maxLength: Int) : PendingInspectionSlotRule {
    override fun check(item: PendingInspectionSlot): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection slot. */
data class PendingInspectionSlotValidator(var rules: MutableList<PendingInspectionSlotRule>) {
    fun validate(item: PendingInspectionSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingInspectionSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionSlotValidator(): PendingInspectionSlotValidator {
    return PendingInspectionSlotValidator(
        mutableListOf<PendingInspectionSlotRule>(
            PendingInspectionSlotRequiredCodeRule("pending inspection slot needs this field"),
            PendingInspectionSlotLimitRangeRule(255, 3600),
            PendingInspectionSlotCategoryLengthRule(1000),
            PendingInspectionSlotRequiredRouteCodeRule("pending inspection slot needs this field"),
            PendingInspectionSlotRouteCodeLengthRule(1000),
        ),
    )
}
