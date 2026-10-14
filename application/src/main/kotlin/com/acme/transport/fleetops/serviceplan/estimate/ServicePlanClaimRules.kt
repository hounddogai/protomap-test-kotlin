package com.acme.transport.fleetops.serviceplan.estimate

/** A rule a service plan claim must pass. Check returns its problems, or none. */
interface ServicePlanClaimRule {
    fun check(item: ServicePlanClaim): MutableList<String>
}

data class ServicePlanClaimRequiredCodeRule(var message: String) : ServicePlanClaimRule {
    override fun check(item: ServicePlanClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanClaimRequiredLabelRule(var message: String) : ServicePlanClaimRule {
    override fun check(item: ServicePlanClaim): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanClaimCodePrefixRule(var prefix: String) : ServicePlanClaimRule {
    override fun check(item: ServicePlanClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanClaimRequiredNotesRule(var message: String) : ServicePlanClaimRule {
    override fun check(item: ServicePlanClaim): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanClaimNotesLengthRule(var maxLength: Int) : ServicePlanClaimRule {
    override fun check(item: ServicePlanClaim): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanClaimLabelLengthRule(var maxLength: Int) : ServicePlanClaimRule {
    override fun check(item: ServicePlanClaim): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan claim. */
data class ServicePlanClaimValidator(var rules: MutableList<ServicePlanClaimRule>) {
    fun validate(item: ServicePlanClaim): MutableList<String> {
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

    fun requireValid(item: ServicePlanClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanClaimValidator(): ServicePlanClaimValidator {
    return ServicePlanClaimValidator(
        mutableListOf<ServicePlanClaimRule>(
            ServicePlanClaimRequiredCodeRule("service plan claim needs this field"),
            ServicePlanClaimRequiredLabelRule("service plan claim needs a value"),
            ServicePlanClaimCodePrefixRule("OPS"),
            ServicePlanClaimRequiredNotesRule("service plan claim needs a value"),
            ServicePlanClaimNotesLengthRule(3600),
            ServicePlanClaimLabelLengthRule(32),
        ),
    )
}
