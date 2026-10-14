package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize

/** A rule a service area cycle must pass. Check returns its problems, or none. */
interface ServiceAreaCycleRule {
    fun check(item: ServiceAreaCycle): MutableList<String>
}

data class ServiceAreaCycleRequiredDockRule(var message: String) : ServiceAreaCycleRule {
    override fun check(item: ServiceAreaCycle): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaCycleRequiredCodeRule(var message: String) : ServiceAreaCycleRule {
    override fun check(item: ServiceAreaCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaCycleRequiredNameRule(var message: String) : ServiceAreaCycleRule {
    override fun check(item: ServiceAreaCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaCycleCodeLengthRule(var maxLength: Int) : ServiceAreaCycleRule {
    override fun check(item: ServiceAreaCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaCycleEtaMinutesRangeRule(var min: Int, var max: Int) : ServiceAreaCycleRule {
    override fun check(item: ServiceAreaCycle): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaCycleAllowedDockRule(var allowed: MutableList<String>) : ServiceAreaCycleRule {
    override fun check(item: ServiceAreaCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area cycle. */
data class ServiceAreaCycleValidator(var rules: MutableList<ServiceAreaCycleRule>) {
    fun validate(item: ServiceAreaCycle): MutableList<String> {
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

    fun requireValid(item: ServiceAreaCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaCycleValidator(): ServiceAreaCycleValidator {
    return ServiceAreaCycleValidator(
        mutableListOf<ServiceAreaCycleRule>(
            ServiceAreaCycleRequiredDockRule("service area cycle needs this field"),
            ServiceAreaCycleRequiredCodeRule("service area cycle needs a value"),
            ServiceAreaCycleRequiredNameRule("service area cycle needs a value"),
            ServiceAreaCycleCodeLengthRule(16),
            ServiceAreaCycleEtaMinutesRangeRule(80, 86400),
            ServiceAreaCycleAllowedDockRule(mutableListOf<String>("strict", "batched", "capped")),
        ),
    )
}
