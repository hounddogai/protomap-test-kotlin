package com.acme.transport.fleetops.serviceplan.target

/** A rule a service plan agreement must pass. Check returns its problems, or none. */
interface ServicePlanAgreementRule {
    fun check(item: ServicePlanAgreement): MutableList<String>
}

data class ServicePlanAgreementLabelLengthRule(var maxLength: Int) : ServicePlanAgreementRule {
    override fun check(item: ServicePlanAgreement): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAgreementRequiredUnitRule(var message: String) : ServicePlanAgreementRule {
    override fun check(item: ServicePlanAgreement): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanAgreementVisibleGuardRule(var limit: Int) : ServicePlanAgreementRule {
    override fun check(item: ServicePlanAgreement): MutableList<String> {
        if (item.visible && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan agreement. */
data class ServicePlanAgreementValidator(var rules: MutableList<ServicePlanAgreementRule>) {
    fun validate(item: ServicePlanAgreement): MutableList<String> {
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

    fun requireValid(item: ServicePlanAgreement) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanAgreement>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanAgreementValidator(): ServicePlanAgreementValidator {
    return ServicePlanAgreementValidator(
        mutableListOf<ServicePlanAgreementRule>(
            ServicePlanAgreementLabelLengthRule(3600),
            ServicePlanAgreementRequiredUnitRule("service plan agreement needs an entry"),
            ServicePlanAgreementVisibleGuardRule(255),
        ),
    )
}
