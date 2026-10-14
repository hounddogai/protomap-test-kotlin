package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.normalize

/** A rule a pending hold tier must pass. Check returns its problems, or none. */
interface PendingHoldTierRule {
    fun check(item: PendingHoldTier): MutableList<String>
}

data class PendingHoldTierRequiredTimezoneRule(var message: String) : PendingHoldTierRule {
    override fun check(item: PendingHoldTier): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldTierCodePrefixRule(var prefix: String) : PendingHoldTierRule {
    override fun check(item: PendingHoldTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldTierRequiredLabelRule(var message: String) : PendingHoldTierRule {
    override fun check(item: PendingHoldTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldTierRequiredLocaleRule(var message: String) : PendingHoldTierRule {
    override fun check(item: PendingHoldTier): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldTierAllowedTimezoneRule(var allowed: MutableList<String>) : PendingHoldTierRule {
    override fun check(item: PendingHoldTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is missing")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending hold tier. */
data class PendingHoldTierValidator(var rules: MutableList<PendingHoldTierRule>) {
    fun validate(item: PendingHoldTier): MutableList<String> {
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

    fun requireValid(item: PendingHoldTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingHoldTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingHoldTierValidator(): PendingHoldTierValidator {
    return PendingHoldTierValidator(
        mutableListOf<PendingHoldTierRule>(
            PendingHoldTierRequiredTimezoneRule("pending hold tier needs an entry"),
            PendingHoldTierCodePrefixRule("REF"),
            PendingHoldTierRequiredLabelRule("pending hold tier needs a value"),
            PendingHoldTierRequiredLocaleRule("pending hold tier needs this field"),
            PendingHoldTierAllowedTimezoneRule(mutableListOf<String>("lenient")),
        ),
    )
}
