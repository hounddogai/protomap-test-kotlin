package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.text.normalize

/** A rule a service area override must pass. Check returns its problems, or none. */
interface ServiceAreaOverrideRule {
    fun check(item: ServiceAreaOverride): MutableList<String>
}

data class ServiceAreaOverrideRequiredSegmentRule(var message: String) : ServiceAreaOverrideRule {
    override fun check(item: ServiceAreaOverride): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaOverrideRequiredNotesRule(var message: String) : ServiceAreaOverrideRule {
    override fun check(item: ServiceAreaOverride): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaOverrideAllowedSegmentRule(var allowed: MutableList<String>) : ServiceAreaOverrideRule {
    override fun check(item: ServiceAreaOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.segment))) {
            return mutableListOf<String>("segment ${item.segment} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaOverrideCodeLengthRule(var maxLength: Int) : ServiceAreaOverrideRule {
    override fun check(item: ServiceAreaOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaOverrideGraceDaysRangeRule(var min: Int, var max: Int) : ServiceAreaOverrideRule {
    override fun check(item: ServiceAreaOverride): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area override. */
data class ServiceAreaOverrideValidator(var rules: MutableList<ServiceAreaOverrideRule>) {
    fun validate(item: ServiceAreaOverride): MutableList<String> {
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

    fun requireValid(item: ServiceAreaOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaOverrideValidator(): ServiceAreaOverrideValidator {
    return ServiceAreaOverrideValidator(
        mutableListOf<ServiceAreaOverrideRule>(
            ServiceAreaOverrideRequiredSegmentRule("service area override needs an entry"),
            ServiceAreaOverrideRequiredNotesRule("service area override needs a value"),
            ServiceAreaOverrideAllowedSegmentRule(mutableListOf<String>("lenient", "rolling", "scheduled")),
            ServiceAreaOverrideCodeLengthRule(64),
            ServiceAreaOverrideGraceDaysRangeRule(80, 255),
        ),
    )
}
