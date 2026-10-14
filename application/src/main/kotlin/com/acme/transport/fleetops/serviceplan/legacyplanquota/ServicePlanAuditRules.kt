package com.acme.transport.fleetops.serviceplan.legacyplanquota

/** A rule a service plan audit must pass. Check returns its problems, or none. */
interface ServicePlanAuditRule {
    fun check(item: ServicePlanAudit): MutableList<String>
}

data class ServicePlanAuditRequiredNameRule(var message: String) : ServicePlanAuditRule {
    override fun check(item: ServicePlanAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAuditNameLengthRule(var maxLength: Int) : ServicePlanAuditRule {
    override fun check(item: ServicePlanAudit): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAuditCodePrefixRule(var prefix: String) : ServicePlanAuditRule {
    override fun check(item: ServicePlanAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAuditCodeLengthRule(var maxLength: Int) : ServicePlanAuditRule {
    override fun check(item: ServicePlanAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAuditRequiredShiftCodeRule(var message: String) : ServicePlanAuditRule {
    override fun check(item: ServicePlanAudit): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan audit. */
data class ServicePlanAuditValidator(var rules: MutableList<ServicePlanAuditRule>) {
    fun validate(item: ServicePlanAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanAuditValidator(): ServicePlanAuditValidator {
    return ServicePlanAuditValidator(
        mutableListOf<ServicePlanAuditRule>(
            ServicePlanAuditRequiredNameRule("service plan audit needs an entry"),
            ServicePlanAuditNameLengthRule(86400),
            ServicePlanAuditCodePrefixRule("ACM"),
            ServicePlanAuditCodeLengthRule(1000),
            ServicePlanAuditRequiredShiftCodeRule("service plan audit needs a value"),
        ),
    )
}
