package com.acme.transport.fleetops.serviceplan.target

/** A rule a service plan target must pass. Check returns its problems, or none. */
interface ServicePlanTargetRule {
    fun check(item: ServicePlanTarget): MutableList<String>
}

data class ServicePlanTargetRequiredNotesRule(var message: String) : ServicePlanTargetRule {
    override fun check(item: ServicePlanTarget): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTargetPinnedGuardRule(var limit: Double) : ServicePlanTargetRule {
    override fun check(item: ServicePlanTarget): MutableList<String> {
        if (item.pinned && item.margin > limit) {
            return mutableListOf<String>("margin ${item.margin} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTargetRequiredCodeRule(var message: String) : ServicePlanTargetRule {
    override fun check(item: ServicePlanTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTargetCodePrefixRule(var prefix: String) : ServicePlanTargetRule {
    override fun check(item: ServicePlanTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTargetCodeLengthRule(var maxLength: Int) : ServicePlanTargetRule {
    override fun check(item: ServicePlanTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanTargetRequiredNameRule(var message: String) : ServicePlanTargetRule {
    override fun check(item: ServicePlanTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan target. */
data class ServicePlanTargetValidator(var rules: MutableList<ServicePlanTargetRule>) {
    fun validate(item: ServicePlanTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanTargetValidator(): ServicePlanTargetValidator {
    return ServicePlanTargetValidator(
        mutableListOf<ServicePlanTargetRule>(
            ServicePlanTargetRequiredNotesRule("service plan target needs an entry"),
            ServicePlanTargetPinnedGuardRule(2.9),
            ServicePlanTargetRequiredCodeRule("service plan target needs this field"),
            ServicePlanTargetCodePrefixRule("Q"),
            ServicePlanTargetCodeLengthRule(16),
            ServicePlanTargetRequiredNameRule("service plan target needs an entry"),
        ),
    )
}
