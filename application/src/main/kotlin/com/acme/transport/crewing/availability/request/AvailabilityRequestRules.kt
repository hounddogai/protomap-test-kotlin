package com.acme.transport.crewing.availability.request

import com.acme.transport.kit.text.normalize

/** A rule a availability request must pass. Check returns its problems, or none. */
interface AvailabilityRequestRule {
    fun check(item: AvailabilityRequest): MutableList<String>
}

data class AvailabilityRequestRequiredCodeRule(var message: String) : AvailabilityRequestRule {
    override fun check(item: AvailabilityRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRequestCodeLengthRule(var maxLength: Int) : AvailabilityRequestRule {
    override fun check(item: AvailabilityRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRequestLabelLengthRule(var maxLength: Int) : AvailabilityRequestRule {
    override fun check(item: AvailabilityRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRequestAllowedLabelRule(var allowed: MutableList<String>) : AvailabilityRequestRule {
    override fun check(item: AvailabilityRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRequestCodePrefixRule(var prefix: String) : AvailabilityRequestRule {
    override fun check(item: AvailabilityRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityRequestActiveGuardRule(var limit: Int) : AvailabilityRequestRule {
    override fun check(item: AvailabilityRequest): MutableList<String> {
        if (item.active && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability request. */
data class AvailabilityRequestValidator(var rules: MutableList<AvailabilityRequestRule>) {
    fun validate(item: AvailabilityRequest): MutableList<String> {
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

    fun requireValid(item: AvailabilityRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityRequestValidator(): AvailabilityRequestValidator {
    return AvailabilityRequestValidator(
        mutableListOf<AvailabilityRequestRule>(
            AvailabilityRequestRequiredCodeRule("availability request needs this field"),
            AvailabilityRequestCodeLengthRule(3600),
            AvailabilityRequestLabelLengthRule(86400),
            AvailabilityRequestAllowedLabelRule(mutableListOf<String>("fallback", "primary", "strict")),
            AvailabilityRequestCodePrefixRule("OPS"),
            AvailabilityRequestActiveGuardRule(120),
        ),
    )
}
