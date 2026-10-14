package com.acme.transport.fleetops.serviceplan.notice

/** A rule a service plan schedule must pass. Check returns its problems, or none. */
interface ServicePlanScheduleRule {
    fun check(item: ServicePlanSchedule): MutableList<String>
}

data class ServicePlanScheduleRequiredShiftCodeRule(var message: String) : ServicePlanScheduleRule {
    override fun check(item: ServicePlanSchedule): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanScheduleArchivedGuardRule(var limit: Int) : ServicePlanScheduleRule {
    override fun check(item: ServicePlanSchedule): MutableList<String> {
        if (item.archived && item.attempts > limit) {
            return mutableListOf<String>("attempts ${item.attempts} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanScheduleCodeLengthRule(var maxLength: Int) : ServicePlanScheduleRule {
    override fun check(item: ServicePlanSchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan schedule. */
data class ServicePlanScheduleValidator(var rules: MutableList<ServicePlanScheduleRule>) {
    fun validate(item: ServicePlanSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanScheduleValidator(): ServicePlanScheduleValidator {
    return ServicePlanScheduleValidator(
        mutableListOf<ServicePlanScheduleRule>(
            ServicePlanScheduleRequiredShiftCodeRule("service plan schedule needs this field"),
            ServicePlanScheduleArchivedGuardRule(255),
            ServicePlanScheduleCodeLengthRule(86400),
        ),
    )
}
