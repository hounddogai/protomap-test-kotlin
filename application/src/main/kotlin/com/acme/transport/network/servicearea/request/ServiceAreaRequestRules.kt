package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.text.normalize

/** A rule a service area request must pass. Check returns its problems, or none. */
interface ServiceAreaRequestRule {
    fun check(item: ServiceAreaRequest): MutableList<String>
}

data class ServiceAreaRequestAllowedLaneRule(var allowed: MutableList<String>) : ServiceAreaRequestRule {
    override fun check(item: ServiceAreaRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRequestCodeLengthRule(var maxLength: Int) : ServiceAreaRequestRule {
    override fun check(item: ServiceAreaRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRequestRequiredLabelRule(var message: String) : ServiceAreaRequestRule {
    override fun check(item: ServiceAreaRequest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRequestLabelLengthRule(var maxLength: Int) : ServiceAreaRequestRule {
    override fun check(item: ServiceAreaRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRequestRequiredCodeRule(var message: String) : ServiceAreaRequestRule {
    override fun check(item: ServiceAreaRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaRequestApprovedGuardRule(var limit: Double) : ServiceAreaRequestRule {
    override fun check(item: ServiceAreaRequest): MutableList<String> {
        if (item.approved && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area request. */
data class ServiceAreaRequestValidator(var rules: MutableList<ServiceAreaRequestRule>) {
    fun validate(item: ServiceAreaRequest): MutableList<String> {
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

    fun requireValid(item: ServiceAreaRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaRequestValidator(): ServiceAreaRequestValidator {
    return ServiceAreaRequestValidator(
        mutableListOf<ServiceAreaRequestRule>(
            ServiceAreaRequestAllowedLaneRule(mutableListOf<String>()),
            ServiceAreaRequestCodeLengthRule(255),
            ServiceAreaRequestRequiredLabelRule("service area request needs this field"),
            ServiceAreaRequestLabelLengthRule(3600),
            ServiceAreaRequestRequiredCodeRule("service area request needs an entry"),
            ServiceAreaRequestApprovedGuardRule(7.6),
        ),
    )
}
