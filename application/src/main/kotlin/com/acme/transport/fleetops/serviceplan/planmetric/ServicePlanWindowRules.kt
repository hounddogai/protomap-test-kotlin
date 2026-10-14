package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.text.normalize

/** A rule a service plan window must pass. Check returns its problems, or none. */
interface ServicePlanWindowRule {
    fun check(item: ServicePlanWindow): MutableList<String>
}

data class ServicePlanWindowRequiredCodeRule(var message: String) : ServicePlanWindowRule {
    override fun check(item: ServicePlanWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanWindowOdometerRangeRule(var min: Int, var max: Int) : ServicePlanWindowRule {
    override fun check(item: ServicePlanWindow): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanWindowSummaryLengthRule(var maxLength: Int) : ServicePlanWindowRule {
    override fun check(item: ServicePlanWindow): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanWindowCarrierLengthRule(var maxLength: Int) : ServicePlanWindowRule {
    override fun check(item: ServicePlanWindow): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanWindowAllowedNameRule(var allowed: MutableList<String>) : ServicePlanWindowRule {
    override fun check(item: ServicePlanWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanWindowCodeLengthRule(var maxLength: Int) : ServicePlanWindowRule {
    override fun check(item: ServicePlanWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan window. */
data class ServicePlanWindowValidator(var rules: MutableList<ServicePlanWindowRule>) {
    fun validate(item: ServicePlanWindow): MutableList<String> {
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

    fun requireValid(item: ServicePlanWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanWindowValidator(): ServicePlanWindowValidator {
    return ServicePlanWindowValidator(
        mutableListOf<ServicePlanWindowRule>(
            ServicePlanWindowRequiredCodeRule("service plan window needs a value"),
            ServicePlanWindowOdometerRangeRule(16, 80),
            ServicePlanWindowSummaryLengthRule(64),
            ServicePlanWindowCarrierLengthRule(86400),
            ServicePlanWindowAllowedNameRule(mutableListOf<String>("lenient")),
            ServicePlanWindowCodeLengthRule(255),
        ),
    )
}
