package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.normalize

/** A rule a service area claim must pass. Check returns its problems, or none. */
interface ServiceAreaClaimRule {
    fun check(item: ServiceAreaClaim): MutableList<String>
}

data class ServiceAreaClaimCodePrefixRule(var prefix: String) : ServiceAreaClaimRule {
    override fun check(item: ServiceAreaClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaClaimRequiredTitleRule(var message: String) : ServiceAreaClaimRule {
    override fun check(item: ServiceAreaClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaClaimAllowedTimezoneRule(var allowed: MutableList<String>) : ServiceAreaClaimRule {
    override fun check(item: ServiceAreaClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area claim. */
data class ServiceAreaClaimValidator(var rules: MutableList<ServiceAreaClaimRule>) {
    fun validate(item: ServiceAreaClaim): MutableList<String> {
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

    fun requireValid(item: ServiceAreaClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaClaimValidator(): ServiceAreaClaimValidator {
    return ServiceAreaClaimValidator(
        mutableListOf<ServiceAreaClaimRule>(
            ServiceAreaClaimCodePrefixRule("X"),
            ServiceAreaClaimRequiredTitleRule("service area claim needs an entry"),
            ServiceAreaClaimAllowedTimezoneRule(mutableListOf<String>()),
        ),
    )
}
