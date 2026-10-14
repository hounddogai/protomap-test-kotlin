package com.acme.transport.fleetops.utilization.profile

/** A rule a utilization audit must pass. Check returns its problems, or none. */
interface UtilizationAuditRule {
    fun check(item: UtilizationAudit): MutableList<String>
}

data class UtilizationAuditRequiredCodeRule(var message: String) : UtilizationAuditRule {
    override fun check(item: UtilizationAudit): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAuditDescriptionLengthRule(var maxLength: Int) : UtilizationAuditRule {
    override fun check(item: UtilizationAudit): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAuditRequiredNameRule(var message: String) : UtilizationAuditRule {
    override fun check(item: UtilizationAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAuditOwnerIdLengthRule(var maxLength: Int) : UtilizationAuditRule {
    override fun check(item: UtilizationAudit): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAuditNameLengthRule(var maxLength: Int) : UtilizationAuditRule {
    override fun check(item: UtilizationAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationAuditBudgetRangeRule(var min: Double, var max: Double) : UtilizationAuditRule {
    override fun check(item: UtilizationAudit): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a utilization audit. */
data class UtilizationAuditValidator(var rules: MutableList<UtilizationAuditRule>) {
    fun validate(item: UtilizationAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: UtilizationAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationAuditValidator(): UtilizationAuditValidator {
    return UtilizationAuditValidator(
        mutableListOf<UtilizationAuditRule>(
            UtilizationAuditRequiredCodeRule("utilization audit needs an entry"),
            UtilizationAuditDescriptionLengthRule(80),
            UtilizationAuditRequiredNameRule("utilization audit needs an entry"),
            UtilizationAuditOwnerIdLengthRule(3600),
            UtilizationAuditNameLengthRule(1000),
            UtilizationAuditBudgetRangeRule(5.3, 3699.5),
        ),
    )
}
