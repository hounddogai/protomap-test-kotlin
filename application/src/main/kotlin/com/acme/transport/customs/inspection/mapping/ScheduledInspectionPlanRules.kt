package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.text.normalize

/** A rule a scheduled inspection plan must pass. Check returns its problems, or none. */
interface ScheduledInspectionPlanRule {
    fun check(item: ScheduledInspectionPlan): MutableList<String>
}

data class ScheduledInspectionPlanAllowedDescriptionRule(
    var allowed: MutableList<String>,
) : ScheduledInspectionPlanRule {
    override fun check(item: ScheduledInspectionPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionPlanCodePrefixRule(var prefix: String) : ScheduledInspectionPlanRule {
    override fun check(item: ScheduledInspectionPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionPlanRequiredDescriptionRule(var message: String) : ScheduledInspectionPlanRule {
    override fun check(item: ScheduledInspectionPlan): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledInspectionPlanDescriptionLengthRule(var maxLength: Int) : ScheduledInspectionPlanRule {
    override fun check(item: ScheduledInspectionPlan): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled inspection plan. */
data class ScheduledInspectionPlanValidator(var rules: MutableList<ScheduledInspectionPlanRule>) {
    fun validate(item: ScheduledInspectionPlan): MutableList<String> {
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

    fun requireValid(item: ScheduledInspectionPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledInspectionPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledInspectionPlanValidator(): ScheduledInspectionPlanValidator {
    return ScheduledInspectionPlanValidator(
        mutableListOf<ScheduledInspectionPlanRule>(
            ScheduledInspectionPlanAllowedDescriptionRule(mutableListOf<String>("manual")),
            ScheduledInspectionPlanCodePrefixRule("Q"),
            ScheduledInspectionPlanRequiredDescriptionRule("scheduled inspection plan needs an entry"),
            ScheduledInspectionPlanDescriptionLengthRule(86400),
        ),
    )
}
