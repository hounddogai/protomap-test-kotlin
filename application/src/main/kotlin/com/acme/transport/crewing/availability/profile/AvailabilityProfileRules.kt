package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize

/** A rule a availability profile must pass. Check returns its problems, or none. */
interface AvailabilityProfileRule {
    fun check(item: AvailabilityProfile): MutableList<String>
}

data class AvailabilityProfileCodeLengthRule(var maxLength: Int) : AvailabilityProfileRule {
    override fun check(item: AvailabilityProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProfileRequiredOwnerIdRule(var message: String) : AvailabilityProfileRule {
    override fun check(item: AvailabilityProfile): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProfileAllowedTitleRule(var allowed: MutableList<String>) : AvailabilityProfileRule {
    override fun check(item: AvailabilityProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProfileTitleLengthRule(var maxLength: Int) : AvailabilityProfileRule {
    override fun check(item: AvailabilityProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityProfileCodePrefixRule(var prefix: String) : AvailabilityProfileRule {
    override fun check(item: AvailabilityProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability profile. */
data class AvailabilityProfileValidator(var rules: MutableList<AvailabilityProfileRule>) {
    fun validate(item: AvailabilityProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AvailabilityProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityProfileValidator(): AvailabilityProfileValidator {
    return AvailabilityProfileValidator(
        mutableListOf<AvailabilityProfileRule>(
            AvailabilityProfileCodeLengthRule(64),
            AvailabilityProfileRequiredOwnerIdRule("availability profile needs an entry"),
            AvailabilityProfileAllowedTitleRule(mutableListOf<String>()),
            AvailabilityProfileTitleLengthRule(64),
            AvailabilityProfileCodePrefixRule("X"),
        ),
    )
}
