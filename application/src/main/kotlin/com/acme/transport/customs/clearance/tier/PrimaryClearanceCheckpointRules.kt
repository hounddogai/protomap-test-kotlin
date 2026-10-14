package com.acme.transport.customs.clearance.tier

import com.acme.transport.kit.text.normalize

/** A rule a primary clearance checkpoint must pass. Check returns its problems, or none. */
interface PrimaryClearanceCheckpointRule {
    fun check(item: PrimaryClearanceCheckpoint): MutableList<String>
}

data class PrimaryClearanceCheckpointNameLengthRule(var maxLength: Int) : PrimaryClearanceCheckpointRule {
    override fun check(item: PrimaryClearanceCheckpoint): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceCheckpointAllowedLocaleRule(
    var allowed: MutableList<String>,
) : PrimaryClearanceCheckpointRule {
    override fun check(item: PrimaryClearanceCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is missing")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceCheckpointRequiredCodeRule(var message: String) : PrimaryClearanceCheckpointRule {
    override fun check(item: PrimaryClearanceCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceCheckpointLocaleLengthRule(var maxLength: Int) : PrimaryClearanceCheckpointRule {
    override fun check(item: PrimaryClearanceCheckpoint): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceCheckpointRequiredCarrierRule(var message: String) : PrimaryClearanceCheckpointRule {
    override fun check(item: PrimaryClearanceCheckpoint): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceCheckpointCodePrefixRule(var prefix: String) : PrimaryClearanceCheckpointRule {
    override fun check(item: PrimaryClearanceCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary clearance checkpoint. */
data class PrimaryClearanceCheckpointValidator(var rules: MutableList<PrimaryClearanceCheckpointRule>) {
    fun validate(item: PrimaryClearanceCheckpoint): MutableList<String> {
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

    fun requireValid(item: PrimaryClearanceCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryClearanceCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryClearanceCheckpointValidator(): PrimaryClearanceCheckpointValidator {
    return PrimaryClearanceCheckpointValidator(
        mutableListOf<PrimaryClearanceCheckpointRule>(
            PrimaryClearanceCheckpointNameLengthRule(255),
            PrimaryClearanceCheckpointAllowedLocaleRule(mutableListOf<String>("tiered", "fallback")),
            PrimaryClearanceCheckpointRequiredCodeRule("primary clearance checkpoint needs an entry"),
            PrimaryClearanceCheckpointLocaleLengthRule(255),
            PrimaryClearanceCheckpointRequiredCarrierRule("primary clearance checkpoint needs a value"),
            PrimaryClearanceCheckpointCodePrefixRule("ACM"),
        ),
    )
}
