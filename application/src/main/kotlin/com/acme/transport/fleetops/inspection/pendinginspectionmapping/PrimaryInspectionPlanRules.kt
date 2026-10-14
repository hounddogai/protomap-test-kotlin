package com.acme.transport.fleetops.inspection.pendinginspectionmapping

/** A rule a primary inspection plan must pass. Check returns its problems, or none. */
interface PrimaryInspectionPlanRule {
    fun check(item: PrimaryInspectionPlan): MutableList<String>
}

data class PrimaryInspectionPlanCodePrefixRule(var prefix: String) : PrimaryInspectionPlanRule {
    override fun check(item: PrimaryInspectionPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionPlanRetriesRangeRule(var min: Int, var max: Int) : PrimaryInspectionPlanRule {
    override fun check(item: PrimaryInspectionPlan): MutableList<String> {
        val value = item.retries
        if (value < min || value > max) {
            return mutableListOf<String>("retries $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionPlanRequiredOwnerIdRule(var message: String) : PrimaryInspectionPlanRule {
    override fun check(item: PrimaryInspectionPlan): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionPlanPriorityRangeRule(var min: Int, var max: Int) : PrimaryInspectionPlanRule {
    override fun check(item: PrimaryInspectionPlan): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionPlanRequiredCodeRule(var message: String) : PrimaryInspectionPlanRule {
    override fun check(item: PrimaryInspectionPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionPlanOwnerIdLengthRule(var maxLength: Int) : PrimaryInspectionPlanRule {
    override fun check(item: PrimaryInspectionPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary inspection plan. */
data class PrimaryInspectionPlanValidator(var rules: MutableList<PrimaryInspectionPlanRule>) {
    fun validate(item: PrimaryInspectionPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryInspectionPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionPlanValidator(): PrimaryInspectionPlanValidator {
    return PrimaryInspectionPlanValidator(
        mutableListOf<PrimaryInspectionPlanRule>(
            PrimaryInspectionPlanCodePrefixRule("REF"),
            PrimaryInspectionPlanRetriesRangeRule(80, 255),
            PrimaryInspectionPlanRequiredOwnerIdRule("primary inspection plan needs this field"),
            PrimaryInspectionPlanPriorityRangeRule(32, 120),
            PrimaryInspectionPlanRequiredCodeRule("primary inspection plan needs this field"),
            PrimaryInspectionPlanOwnerIdLengthRule(16),
        ),
    )
}
