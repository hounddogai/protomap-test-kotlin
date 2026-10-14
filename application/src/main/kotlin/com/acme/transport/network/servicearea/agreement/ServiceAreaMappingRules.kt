package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.text.normalize

/** A rule a service area mapping must pass. Check returns its problems, or none. */
interface ServiceAreaMappingRule {
    fun check(item: ServiceAreaMapping): MutableList<String>
}

data class ServiceAreaMappingAllowedCarrierRule(var allowed: MutableList<String>) : ServiceAreaMappingRule {
    override fun check(item: ServiceAreaMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaMappingRequiredChecksumRule(var message: String) : ServiceAreaMappingRule {
    override fun check(item: ServiceAreaMapping): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaMappingCodeLengthRule(var maxLength: Int) : ServiceAreaMappingRule {
    override fun check(item: ServiceAreaMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaMappingRequiredCodeRule(var message: String) : ServiceAreaMappingRule {
    override fun check(item: ServiceAreaMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area mapping. */
data class ServiceAreaMappingValidator(var rules: MutableList<ServiceAreaMappingRule>) {
    fun validate(item: ServiceAreaMapping): MutableList<String> {
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

    fun requireValid(item: ServiceAreaMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaMappingValidator(): ServiceAreaMappingValidator {
    return ServiceAreaMappingValidator(
        mutableListOf<ServiceAreaMappingRule>(
            ServiceAreaMappingAllowedCarrierRule(mutableListOf<String>("legacy")),
            ServiceAreaMappingRequiredChecksumRule("service area mapping needs a value"),
            ServiceAreaMappingCodeLengthRule(86400),
            ServiceAreaMappingRequiredCodeRule("service area mapping needs this field"),
        ),
    )
}
