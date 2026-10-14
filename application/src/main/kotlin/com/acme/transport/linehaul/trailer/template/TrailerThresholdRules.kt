package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.text.normalize

/** A rule a trailer threshold must pass. Check returns its problems, or none. */
interface TrailerThresholdRule {
    fun check(item: TrailerThreshold): MutableList<String>
}

data class TrailerThresholdCodePrefixRule(var prefix: String) : TrailerThresholdRule {
    override fun check(item: TrailerThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerThresholdAllowedOwnerIdRule(var allowed: MutableList<String>) : TrailerThresholdRule {
    override fun check(item: TrailerThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class TrailerThresholdContactEmailLengthRule(var maxLength: Int) : TrailerThresholdRule {
    override fun check(item: TrailerThreshold): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerThresholdOwnerIdLengthRule(var maxLength: Int) : TrailerThresholdRule {
    override fun check(item: TrailerThreshold): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerThresholdRequiredCodeRule(var message: String) : TrailerThresholdRule {
    override fun check(item: TrailerThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer threshold. */
data class TrailerThresholdValidator(var rules: MutableList<TrailerThresholdRule>) {
    fun validate(item: TrailerThreshold): MutableList<String> {
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

    fun requireValid(item: TrailerThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerThresholdValidator(): TrailerThresholdValidator {
    return TrailerThresholdValidator(
        mutableListOf<TrailerThresholdRule>(
            TrailerThresholdCodePrefixRule("X"),
            TrailerThresholdAllowedOwnerIdRule(mutableListOf<String>("strict", "scheduled")),
            TrailerThresholdContactEmailLengthRule(64),
            TrailerThresholdOwnerIdLengthRule(16),
            TrailerThresholdRequiredCodeRule("trailer threshold needs this field"),
        ),
    )
}
