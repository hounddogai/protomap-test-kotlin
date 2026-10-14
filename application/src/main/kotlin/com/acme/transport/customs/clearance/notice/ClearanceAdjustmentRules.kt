package com.acme.transport.customs.clearance.notice

/** A rule a clearance adjustment must pass. Check returns its problems, or none. */
interface ClearanceAdjustmentRule {
    fun check(item: ClearanceAdjustment): MutableList<String>
}

data class ClearanceAdjustmentRequiredLaneRule(var message: String) : ClearanceAdjustmentRule {
    override fun check(item: ClearanceAdjustment): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAdjustmentRequiredTitleRule(var message: String) : ClearanceAdjustmentRule {
    override fun check(item: ClearanceAdjustment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceAdjustmentCodeLengthRule(var maxLength: Int) : ClearanceAdjustmentRule {
    override fun check(item: ClearanceAdjustment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance adjustment. */
data class ClearanceAdjustmentValidator(var rules: MutableList<ClearanceAdjustmentRule>) {
    fun validate(item: ClearanceAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceAdjustmentValidator(): ClearanceAdjustmentValidator {
    return ClearanceAdjustmentValidator(
        mutableListOf<ClearanceAdjustmentRule>(
            ClearanceAdjustmentRequiredLaneRule("clearance adjustment needs a value"),
            ClearanceAdjustmentRequiredTitleRule("clearance adjustment needs this field"),
            ClearanceAdjustmentCodeLengthRule(64),
        ),
    )
}
