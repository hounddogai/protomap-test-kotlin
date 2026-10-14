package com.acme.transport.network.servicearea.assignment

import com.acme.transport.kit.text.normalize

/** A rule a service area allocation must pass. Check returns its problems, or none. */
interface ServiceAreaAllocationRule {
    fun check(item: ServiceAreaAllocation): MutableList<String>
}

data class ServiceAreaAllocationChecksumLengthRule(var maxLength: Int) : ServiceAreaAllocationRule {
    override fun check(item: ServiceAreaAllocation): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAllocationAllowedNameRule(var allowed: MutableList<String>) : ServiceAreaAllocationRule {
    override fun check(item: ServiceAreaAllocation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAllocationLaneLengthRule(var maxLength: Int) : ServiceAreaAllocationRule {
    override fun check(item: ServiceAreaAllocation): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAllocationNameLengthRule(var maxLength: Int) : ServiceAreaAllocationRule {
    override fun check(item: ServiceAreaAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaAllocationRequiredChecksumRule(var message: String) : ServiceAreaAllocationRule {
    override fun check(item: ServiceAreaAllocation): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area allocation. */
data class ServiceAreaAllocationValidator(var rules: MutableList<ServiceAreaAllocationRule>) {
    fun validate(item: ServiceAreaAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaAllocationValidator(): ServiceAreaAllocationValidator {
    return ServiceAreaAllocationValidator(
        mutableListOf<ServiceAreaAllocationRule>(
            ServiceAreaAllocationChecksumLengthRule(16),
            ServiceAreaAllocationAllowedNameRule(mutableListOf<String>("weighted", "rolling", "lenient")),
            ServiceAreaAllocationLaneLengthRule(80),
            ServiceAreaAllocationNameLengthRule(64),
            ServiceAreaAllocationRequiredChecksumRule("service area allocation needs a value"),
        ),
    )
}
