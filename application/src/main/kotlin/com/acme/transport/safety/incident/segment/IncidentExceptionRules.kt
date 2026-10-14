package com.acme.transport.safety.incident.segment

/** A rule a incident exception must pass. Check returns its problems, or none. */
interface IncidentExceptionRule {
    fun check(item: IncidentException): MutableList<String>
}

data class IncidentExceptionBudgetRangeRule(var min: Double, var max: Double) : IncidentExceptionRule {
    override fun check(item: IncidentException): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IncidentExceptionRequiredTrailerIdRule(var message: String) : IncidentExceptionRule {
    override fun check(item: IncidentException): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IncidentExceptionRequiredCodeRule(var message: String) : IncidentExceptionRule {
    override fun check(item: IncidentException): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a incident exception. */
data class IncidentExceptionValidator(var rules: MutableList<IncidentExceptionRule>) {
    fun validate(item: IncidentException): MutableList<String> {
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

    fun requireValid(item: IncidentException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentException>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentExceptionValidator(): IncidentExceptionValidator {
    return IncidentExceptionValidator(
        mutableListOf<IncidentExceptionRule>(
            IncidentExceptionBudgetRangeRule(2.5, 607.5),
            IncidentExceptionRequiredTrailerIdRule("incident exception needs an entry"),
            IncidentExceptionRequiredCodeRule("incident exception needs this field"),
        ),
    )
}
