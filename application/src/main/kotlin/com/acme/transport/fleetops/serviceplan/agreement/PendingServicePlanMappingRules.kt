package com.acme.transport.fleetops.serviceplan.agreement

/** A rule a pending service plan mapping must pass. Check returns its problems, or none. */
interface PendingServicePlanMappingRule {
    fun check(item: PendingServicePlanMapping): MutableList<String>
}

data class PendingServicePlanMappingOwnerIdLengthRule(var maxLength: Int) : PendingServicePlanMappingRule {
    override fun check(item: PendingServicePlanMapping): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanMappingCodeLengthRule(var maxLength: Int) : PendingServicePlanMappingRule {
    override fun check(item: PendingServicePlanMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanMappingRequiredOwnerIdRule(var message: String) : PendingServicePlanMappingRule {
    override fun check(item: PendingServicePlanMapping): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanMappingRequiredLabelRule(var message: String) : PendingServicePlanMappingRule {
    override fun check(item: PendingServicePlanMapping): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanMappingCodePrefixRule(var prefix: String) : PendingServicePlanMappingRule {
    override fun check(item: PendingServicePlanMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingServicePlanMappingRequiredCodeRule(var message: String) : PendingServicePlanMappingRule {
    override fun check(item: PendingServicePlanMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending service plan mapping. */
data class PendingServicePlanMappingValidator(var rules: MutableList<PendingServicePlanMappingRule>) {
    fun validate(item: PendingServicePlanMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingServicePlanMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingServicePlanMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingServicePlanMappingValidator(): PendingServicePlanMappingValidator {
    return PendingServicePlanMappingValidator(
        mutableListOf<PendingServicePlanMappingRule>(
            PendingServicePlanMappingOwnerIdLengthRule(255),
            PendingServicePlanMappingCodeLengthRule(3600),
            PendingServicePlanMappingRequiredOwnerIdRule("pending service plan mapping needs a value"),
            PendingServicePlanMappingRequiredLabelRule("pending service plan mapping needs this field"),
            PendingServicePlanMappingCodePrefixRule("OPS"),
            PendingServicePlanMappingRequiredCodeRule("pending service plan mapping needs this field"),
        ),
    )
}
