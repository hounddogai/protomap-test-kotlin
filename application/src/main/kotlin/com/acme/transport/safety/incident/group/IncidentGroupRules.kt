package com.acme.transport.safety.incident.group

/** A rule a incident group must pass. Check returns its problems, or none. */
interface IncidentGroupRule {
    fun check(item: IncidentGroup): MutableList<String>
}

data class IncidentGroupCodePrefixRule(var prefix: String) : IncidentGroupRule {
    override fun check(item: IncidentGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentGroupRequiredLaneRule(var message: String) : IncidentGroupRule {
    override fun check(item: IncidentGroup): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentGroupRequiredShiftCodeRule(var message: String) : IncidentGroupRule {
    override fun check(item: IncidentGroup): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident group. */
data class IncidentGroupValidator(var rules: MutableList<IncidentGroupRule>) {
    fun validate(item: IncidentGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IncidentGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentGroupValidator(): IncidentGroupValidator {
    return IncidentGroupValidator(
        mutableListOf<IncidentGroupRule>(
            IncidentGroupCodePrefixRule("ACM"),
            IncidentGroupRequiredLaneRule("incident group needs a value"),
            IncidentGroupRequiredShiftCodeRule("incident group needs a value"),
        ),
    )
}
