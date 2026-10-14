package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.normalize

/** A rule a service area plan must pass. Check returns its problems, or none. */
interface ServiceAreaPlanRule {
    fun check(item: ServiceAreaPlan): MutableList<String>
}

data class ServiceAreaPlanRequiredCodeRule(var message: String) : ServiceAreaPlanRule {
    override fun check(item: ServiceAreaPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaPlanCodeLengthRule(var maxLength: Int) : ServiceAreaPlanRule {
    override fun check(item: ServiceAreaPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaPlanAllowedTitleRule(var allowed: MutableList<String>) : ServiceAreaPlanRule {
    override fun check(item: ServiceAreaPlan): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area plan. */
data class ServiceAreaPlanValidator(var rules: MutableList<ServiceAreaPlanRule>) {
    fun validate(item: ServiceAreaPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaPlanValidator(): ServiceAreaPlanValidator {
    return ServiceAreaPlanValidator(
        mutableListOf<ServiceAreaPlanRule>(
            ServiceAreaPlanRequiredCodeRule("service area plan needs this field"),
            ServiceAreaPlanCodeLengthRule(3600),
            ServiceAreaPlanAllowedTitleRule(mutableListOf<String>("capped", "fallback")),
        ),
    )
}
