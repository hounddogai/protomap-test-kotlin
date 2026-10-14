package com.acme.transport.customs.inspection.plan

/** A rule a pending inspection adjustment must pass. Check returns its problems, or none. */
interface PendingInspectionAdjustmentRule {
    fun check(item: PendingInspectionAdjustment): MutableList<String>
}

data class PendingInspectionAdjustmentRequiredLabelRule(var message: String) : PendingInspectionAdjustmentRule {
    override fun check(item: PendingInspectionAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionAdjustmentRequiredCodeRule(var message: String) : PendingInspectionAdjustmentRule {
    override fun check(item: PendingInspectionAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionAdjustmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PendingInspectionAdjustmentRule {
    override fun check(item: PendingInspectionAdjustment): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class PendingInspectionAdjustmentRequiredDepotRule(var message: String) : PendingInspectionAdjustmentRule {
    override fun check(item: PendingInspectionAdjustment): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection adjustment. */
data class PendingInspectionAdjustmentValidator(var rules: MutableList<PendingInspectionAdjustmentRule>) {
    fun validate(item: PendingInspectionAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingInspectionAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionAdjustmentValidator(): PendingInspectionAdjustmentValidator {
    return PendingInspectionAdjustmentValidator(
        mutableListOf<PendingInspectionAdjustmentRule>(
            PendingInspectionAdjustmentRequiredLabelRule("pending inspection adjustment needs an entry"),
            PendingInspectionAdjustmentRequiredCodeRule("pending inspection adjustment needs this field"),
            PendingInspectionAdjustmentThresholdRangeRule(7.6, 1398.0),
            PendingInspectionAdjustmentRequiredDepotRule("pending inspection adjustment needs an entry"),
        ),
    )
}
