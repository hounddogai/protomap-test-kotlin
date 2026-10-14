package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.normalize

/** A rule a scheduled availability cycle must pass. Check returns its problems, or none. */
interface ScheduledAvailabilityCycleRule {
    fun check(item: ScheduledAvailabilityCycle): MutableList<String>
}

data class ScheduledAvailabilityCycleCodePrefixRule(var prefix: String) : ScheduledAvailabilityCycleRule {
    override fun check(item: ScheduledAvailabilityCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityCycleAllowedCountryRule(
    var allowed: MutableList<String>,
) : ScheduledAvailabilityCycleRule {
    override fun check(item: ScheduledAvailabilityCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityCycleCodeLengthRule(var maxLength: Int) : ScheduledAvailabilityCycleRule {
    override fun check(item: ScheduledAvailabilityCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityCycleRequiredCodeRule(var message: String) : ScheduledAvailabilityCycleRule {
    override fun check(item: ScheduledAvailabilityCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityCycleRequiredNameRule(var message: String) : ScheduledAvailabilityCycleRule {
    override fun check(item: ScheduledAvailabilityCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledAvailabilityCycleRequiredLaneRule(var message: String) : ScheduledAvailabilityCycleRule {
    override fun check(item: ScheduledAvailabilityCycle): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled availability cycle. */
data class ScheduledAvailabilityCycleValidator(var rules: MutableList<ScheduledAvailabilityCycleRule>) {
    fun validate(item: ScheduledAvailabilityCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledAvailabilityCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledAvailabilityCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledAvailabilityCycleValidator(): ScheduledAvailabilityCycleValidator {
    return ScheduledAvailabilityCycleValidator(
        mutableListOf<ScheduledAvailabilityCycleRule>(
            ScheduledAvailabilityCycleCodePrefixRule("ACM"),
            ScheduledAvailabilityCycleAllowedCountryRule(mutableListOf<String>("manual", "pending")),
            ScheduledAvailabilityCycleCodeLengthRule(120),
            ScheduledAvailabilityCycleRequiredCodeRule("scheduled availability cycle needs this field"),
            ScheduledAvailabilityCycleRequiredNameRule("scheduled availability cycle needs a value"),
            ScheduledAvailabilityCycleRequiredLaneRule("scheduled availability cycle needs an entry"),
        ),
    )
}
