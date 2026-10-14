package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.kit.text.normalize

/** A rule a lenient clearance threshold must pass. Check returns its problems, or none. */
interface LenientClearanceThresholdRule {
    fun check(item: LenientClearanceThreshold): MutableList<String>
}

data class LenientClearanceThresholdCodePrefixRule(var prefix: String) : LenientClearanceThresholdRule {
    override fun check(item: LenientClearanceThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceThresholdRequiredCodeRule(var message: String) : LenientClearanceThresholdRule {
    override fun check(item: LenientClearanceThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceThresholdArchivedGuardRule(var limit: Double) : LenientClearanceThresholdRule {
    override fun check(item: LenientClearanceThreshold): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceThresholdRequiredExternalRefRule(
    var message: String,
) : LenientClearanceThresholdRule {
    override fun check(item: LenientClearanceThreshold): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientClearanceThresholdAllowedLabelRule(
    var allowed: MutableList<String>,
) : LenientClearanceThresholdRule {
    override fun check(item: LenientClearanceThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient clearance threshold. */
data class LenientClearanceThresholdValidator(var rules: MutableList<LenientClearanceThresholdRule>) {
    fun validate(item: LenientClearanceThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientClearanceThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientClearanceThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientClearanceThresholdValidator(): LenientClearanceThresholdValidator {
    return LenientClearanceThresholdValidator(
        mutableListOf<LenientClearanceThresholdRule>(
            LenientClearanceThresholdCodePrefixRule("REF"),
            LenientClearanceThresholdRequiredCodeRule("lenient clearance threshold needs an entry"),
            LenientClearanceThresholdArchivedGuardRule(1.8),
            LenientClearanceThresholdRequiredExternalRefRule("lenient clearance threshold needs an entry"),
            LenientClearanceThresholdAllowedLabelRule(mutableListOf<String>()),
        ),
    )
}
