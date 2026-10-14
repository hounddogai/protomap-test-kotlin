package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.normalize

/** A rule a global inspection plan must pass. Check returns its problems, or none. */
interface GlobalInspectionPlanRule {
    fun check(item: GlobalInspectionPlan): MutableList<String>
}

data class GlobalInspectionPlanRequiredCodeRule(var message: String) : GlobalInspectionPlanRule {
    override fun check(item: GlobalInspectionPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionPlanOwnerIdLengthRule(var maxLength: Int) : GlobalInspectionPlanRule {
    override fun check(item: GlobalInspectionPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionPlanRequiredNameRule(var message: String) : GlobalInspectionPlanRule {
    override fun check(item: GlobalInspectionPlan): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionPlanAllowedCodeRule(var allowed: MutableList<String>) : GlobalInspectionPlanRule {
    override fun check(item: GlobalInspectionPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionPlanRequiredLabelRule(var message: String) : GlobalInspectionPlanRule {
    override fun check(item: GlobalInspectionPlan): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionPlanRequiredOwnerIdRule(var message: String) : GlobalInspectionPlanRule {
    override fun check(item: GlobalInspectionPlan): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection plan. */
data class GlobalInspectionPlanValidator(var rules: MutableList<GlobalInspectionPlanRule>) {
    fun validate(item: GlobalInspectionPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalInspectionPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionPlanValidator(): GlobalInspectionPlanValidator {
    return GlobalInspectionPlanValidator(
        mutableListOf<GlobalInspectionPlanRule>(
            GlobalInspectionPlanRequiredCodeRule("global inspection plan needs this field"),
            GlobalInspectionPlanOwnerIdLengthRule(64),
            GlobalInspectionPlanRequiredNameRule("global inspection plan needs a value"),
            GlobalInspectionPlanAllowedCodeRule(mutableListOf<String>()),
            GlobalInspectionPlanRequiredLabelRule("global inspection plan needs an entry"),
            GlobalInspectionPlanRequiredOwnerIdRule("global inspection plan needs an entry"),
        ),
    )
}
