package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize

/** A rule a service area profile must pass. Check returns its problems, or none. */
interface ServiceAreaProfileRule {
    fun check(item: ServiceAreaProfile): MutableList<String>
}

data class ServiceAreaProfileNameLengthRule(var maxLength: Int) : ServiceAreaProfileRule {
    override fun check(item: ServiceAreaProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaProfileRequiredNameRule(var message: String) : ServiceAreaProfileRule {
    override fun check(item: ServiceAreaProfile): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaProfileAllowedDockRule(var allowed: MutableList<String>) : ServiceAreaProfileRule {
    override fun check(item: ServiceAreaProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area profile. */
data class ServiceAreaProfileValidator(var rules: MutableList<ServiceAreaProfileRule>) {
    fun validate(item: ServiceAreaProfile): MutableList<String> {
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

    fun requireValid(item: ServiceAreaProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaProfileValidator(): ServiceAreaProfileValidator {
    return ServiceAreaProfileValidator(
        mutableListOf<ServiceAreaProfileRule>(
            ServiceAreaProfileNameLengthRule(120),
            ServiceAreaProfileRequiredNameRule("service area profile needs this field"),
            ServiceAreaProfileAllowedDockRule(mutableListOf<String>("strict")),
        ),
    )
}
