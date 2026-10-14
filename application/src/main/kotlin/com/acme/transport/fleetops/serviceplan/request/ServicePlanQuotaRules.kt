package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.kit.text.normalize

/** A rule a service plan quota must pass. Check returns its problems, or none. */
interface ServicePlanQuotaRule {
    fun check(item: ServicePlanQuota): MutableList<String>
}

data class ServicePlanQuotaVersionRangeRule(var min: Int, var max: Int) : ServicePlanQuotaRule {
    override fun check(item: ServicePlanQuota): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanQuotaAllowedCodeRule(var allowed: MutableList<String>) : ServicePlanQuotaRule {
    override fun check(item: ServicePlanQuota): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanQuotaSourceLengthRule(var maxLength: Int) : ServicePlanQuotaRule {
    override fun check(item: ServicePlanQuota): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanQuotaRequiredTrailerIdRule(var message: String) : ServicePlanQuotaRule {
    override fun check(item: ServicePlanQuota): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanQuotaTrailerIdLengthRule(var maxLength: Int) : ServicePlanQuotaRule {
    override fun check(item: ServicePlanQuota): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServicePlanQuotaRequiredSourceRule(var message: String) : ServicePlanQuotaRule {
    override fun check(item: ServicePlanQuota): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service plan quota. */
data class ServicePlanQuotaValidator(var rules: MutableList<ServicePlanQuotaRule>) {
    fun validate(item: ServicePlanQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServicePlanQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServicePlanQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultServicePlanQuotaValidator(): ServicePlanQuotaValidator {
    return ServicePlanQuotaValidator(
        mutableListOf<ServicePlanQuotaRule>(
            ServicePlanQuotaVersionRangeRule(120, 3600),
            ServicePlanQuotaAllowedCodeRule(mutableListOf<String>("batched", "legacy")),
            ServicePlanQuotaSourceLengthRule(32),
            ServicePlanQuotaRequiredTrailerIdRule("service plan quota needs an entry"),
            ServicePlanQuotaTrailerIdLengthRule(32),
            ServicePlanQuotaRequiredSourceRule("service plan quota needs an entry"),
        ),
    )
}
