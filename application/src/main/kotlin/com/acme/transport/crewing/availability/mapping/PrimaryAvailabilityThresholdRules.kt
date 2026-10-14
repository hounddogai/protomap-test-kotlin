package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize

/** A rule a primary availability threshold must pass. Check returns its problems, or none. */
interface PrimaryAvailabilityThresholdRule {
    fun check(item: PrimaryAvailabilityThreshold): MutableList<String>
}

data class PrimaryAvailabilityThresholdRequiredCodeRule(
    var message: String,
) : PrimaryAvailabilityThresholdRule {
    override fun check(item: PrimaryAvailabilityThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAvailabilityThresholdArchivedGuardRule(var limit: Int) : PrimaryAvailabilityThresholdRule {
    override fun check(item: PrimaryAvailabilityThreshold): MutableList<String> {
        if (item.archived && item.sequence > limit) {
            return mutableListOf<String>("sequence ${item.sequence} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAvailabilityThresholdCodeLengthRule(var maxLength: Int) : PrimaryAvailabilityThresholdRule {
    override fun check(item: PrimaryAvailabilityThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAvailabilityThresholdAllowedLabelRule(
    var allowed: MutableList<String>,
) : PrimaryAvailabilityThresholdRule {
    override fun check(item: PrimaryAvailabilityThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryAvailabilityThresholdCodePrefixRule(var prefix: String) : PrimaryAvailabilityThresholdRule {
    override fun check(item: PrimaryAvailabilityThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary availability threshold. */
data class PrimaryAvailabilityThresholdValidator(var rules: MutableList<PrimaryAvailabilityThresholdRule>) {
    fun validate(item: PrimaryAvailabilityThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryAvailabilityThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryAvailabilityThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryAvailabilityThresholdValidator(): PrimaryAvailabilityThresholdValidator {
    return PrimaryAvailabilityThresholdValidator(
        mutableListOf<PrimaryAvailabilityThresholdRule>(
            PrimaryAvailabilityThresholdRequiredCodeRule("primary availability threshold needs this field"),
            PrimaryAvailabilityThresholdArchivedGuardRule(32),
            PrimaryAvailabilityThresholdCodeLengthRule(64),
            PrimaryAvailabilityThresholdAllowedLabelRule(mutableListOf<String>("primary")),
            PrimaryAvailabilityThresholdCodePrefixRule("X"),
        ),
    )
}
