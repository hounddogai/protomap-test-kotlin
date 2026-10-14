package com.acme.transport.terminal.dockdoor.ledger

/** A rule a dock door plan must pass. Check returns its problems, or none. */
interface DockDoorPlanRule {
    fun check(item: DockDoorPlan): MutableList<String>
}

data class DockDoorPlanCodeLengthRule(var maxLength: Int) : DockDoorPlanRule {
    override fun check(item: DockDoorPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DockDoorPlanRequiredNameRule(var message: String) : DockDoorPlanRule {
    override fun check(item: DockDoorPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DockDoorPlanRequiredCategoryRule(var message: String) : DockDoorPlanRule {
    override fun check(item: DockDoorPlan): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dock door plan. */
data class DockDoorPlanValidator(var rules: MutableList<DockDoorPlanRule>) {
    fun validate(item: DockDoorPlan): MutableList<String> {
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

    fun requireValid(item: DockDoorPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DockDoorPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultDockDoorPlanValidator(): DockDoorPlanValidator {
    return DockDoorPlanValidator(
        mutableListOf<DockDoorPlanRule>(
            DockDoorPlanCodeLengthRule(255),
            DockDoorPlanRequiredNameRule("dock door plan needs an entry"),
            DockDoorPlanRequiredCategoryRule("dock door plan needs an entry"),
        ),
    )
}
