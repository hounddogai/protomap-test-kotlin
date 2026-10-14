package com.acme.transport.fleetops.serviceplan.rollup

/** A rule a service plan draft must pass. Check returns its problems, or none. */
interface ServicePlanDraftRule {
    fun check(item: ServicePlanDraft): MutableList<String>
}

data class ServicePlanDraftCodeLengthRule(var maxLength: Int) : ServicePlanDraftRule {
    override fun check(item: ServicePlanDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDraftRequiredRouteCodeRule(var message: String) : ServicePlanDraftRule {
    override fun check(item: ServicePlanDraft): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDraftRequiredChecksumRule(var message: String) : ServicePlanDraftRule {
    override fun check(item: ServicePlanDraft): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanDraftLabelLengthRule(var maxLength: Int) : ServicePlanDraftRule {
    override fun check(item: ServicePlanDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan draft. */
data class ServicePlanDraftValidator(var rules: MutableList<ServicePlanDraftRule>) {
    fun validate(item: ServicePlanDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanDraftValidator(): ServicePlanDraftValidator {
    return ServicePlanDraftValidator(
        mutableListOf<ServicePlanDraftRule>(
            ServicePlanDraftCodeLengthRule(32),
            ServicePlanDraftRequiredRouteCodeRule("service plan draft needs this field"),
            ServicePlanDraftRequiredChecksumRule("service plan draft needs this field"),
            ServicePlanDraftLabelLengthRule(255),
        ),
    )
}
