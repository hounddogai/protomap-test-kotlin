package com.acme.transport.safety.incident.segment

/** A rule a incident plan must pass. Check returns its problems, or none. */
interface IncidentPlanRule {
    fun check(item: IncidentPlan): MutableList<String>
}

data class IncidentPlanRequiredCodeRule(var message: String) : IncidentPlanRule {
    override fun check(item: IncidentPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentPlanRequiredVehicleIdRule(var message: String) : IncidentPlanRule {
    override fun check(item: IncidentPlan): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentPlanOwnerIdLengthRule(var maxLength: Int) : IncidentPlanRule {
    override fun check(item: IncidentPlan): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentPlanCodeLengthRule(var maxLength: Int) : IncidentPlanRule {
    override fun check(item: IncidentPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentPlanVehicleIdLengthRule(var maxLength: Int) : IncidentPlanRule {
    override fun check(item: IncidentPlan): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentPlanCodePrefixRule(var prefix: String) : IncidentPlanRule {
    override fun check(item: IncidentPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident plan. */
data class IncidentPlanValidator(var rules: MutableList<IncidentPlanRule>) {
    fun validate(item: IncidentPlan): MutableList<String> {
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

    fun requireValid(item: IncidentPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentPlanValidator(): IncidentPlanValidator {
    return IncidentPlanValidator(
        mutableListOf<IncidentPlanRule>(
            IncidentPlanRequiredCodeRule("incident plan needs an entry"),
            IncidentPlanRequiredVehicleIdRule("incident plan needs an entry"),
            IncidentPlanOwnerIdLengthRule(1000),
            IncidentPlanCodeLengthRule(32),
            IncidentPlanVehicleIdLengthRule(255),
            IncidentPlanCodePrefixRule("REF"),
        ),
    )
}
