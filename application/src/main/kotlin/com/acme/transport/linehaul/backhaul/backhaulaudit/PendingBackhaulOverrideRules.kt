package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize

/** A rule a pending backhaul override must pass. Check returns its problems, or none. */
interface PendingBackhaulOverrideRule {
    fun check(item: PendingBackhaulOverride): MutableList<String>
}

data class PendingBackhaulOverrideRequiredCodeRule(var message: String) : PendingBackhaulOverrideRule {
    override fun check(item: PendingBackhaulOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulOverrideVisibleGuardRule(var limit: Double) : PendingBackhaulOverrideRule {
    override fun check(item: PendingBackhaulOverride): MutableList<String> {
        if (item.visible && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulOverrideStartsAtRangeRule(var min: Int, var max: Int) : PendingBackhaulOverrideRule {
    override fun check(item: PendingBackhaulOverride): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulOverrideAllowedCodeRule(
    var allowed: MutableList<String>,
) : PendingBackhaulOverrideRule {
    override fun check(item: PendingBackhaulOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulOverrideCodePrefixRule(var prefix: String) : PendingBackhaulOverrideRule {
    override fun check(item: PendingBackhaulOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingBackhaulOverrideRequiredCategoryRule(var message: String) : PendingBackhaulOverrideRule {
    override fun check(item: PendingBackhaulOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending backhaul override. */
data class PendingBackhaulOverrideValidator(var rules: MutableList<PendingBackhaulOverrideRule>) {
    fun validate(item: PendingBackhaulOverride): MutableList<String> {
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

    fun requireValid(item: PendingBackhaulOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingBackhaulOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingBackhaulOverrideValidator(): PendingBackhaulOverrideValidator {
    return PendingBackhaulOverrideValidator(
        mutableListOf<PendingBackhaulOverrideRule>(
            PendingBackhaulOverrideRequiredCodeRule("pending backhaul override needs a value"),
            PendingBackhaulOverrideVisibleGuardRule(4.1),
            PendingBackhaulOverrideStartsAtRangeRule(3600, 3600),
            PendingBackhaulOverrideAllowedCodeRule(mutableListOf<String>("lenient", "rolling")),
            PendingBackhaulOverrideCodePrefixRule("X"),
            PendingBackhaulOverrideRequiredCategoryRule("pending backhaul override needs a value"),
        ),
    )
}
