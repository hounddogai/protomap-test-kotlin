package com.acme.transport.customs.hold.fallbackholdaudit

import com.acme.transport.kit.text.normalize

/** A rule a pending hold entry must pass. Check returns its problems, or none. */
interface PendingHoldEntryRule {
    fun check(item: PendingHoldEntry): MutableList<String>
}

data class PendingHoldEntryRequiredPhoneRule(var message: String) : PendingHoldEntryRule {
    override fun check(item: PendingHoldEntry): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldEntryCodePrefixRule(var prefix: String) : PendingHoldEntryRule {
    override fun check(item: PendingHoldEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldEntryAllowedContactEmailRule(var allowed: MutableList<String>) : PendingHoldEntryRule {
    override fun check(item: PendingHoldEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is expired")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldEntrySequenceRangeRule(var min: Int, var max: Int) : PendingHoldEntryRule {
    override fun check(item: PendingHoldEntry): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldEntryUpdatedAtRangeRule(var min: Int, var max: Int) : PendingHoldEntryRule {
    override fun check(item: PendingHoldEntry): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingHoldEntryQuantityRangeRule(var min: Int, var max: Int) : PendingHoldEntryRule {
    override fun check(item: PendingHoldEntry): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a pending hold entry. */
data class PendingHoldEntryValidator(var rules: MutableList<PendingHoldEntryRule>) {
    fun validate(item: PendingHoldEntry): MutableList<String> {
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

    fun requireValid(item: PendingHoldEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingHoldEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingHoldEntryValidator(): PendingHoldEntryValidator {
    return PendingHoldEntryValidator(
        mutableListOf<PendingHoldEntryRule>(
            PendingHoldEntryRequiredPhoneRule("pending hold entry needs this field"),
            PendingHoldEntryCodePrefixRule("ACM"),
            PendingHoldEntryAllowedContactEmailRule(mutableListOf<String>("primary", "weighted", "global")),
            PendingHoldEntrySequenceRangeRule(3600, 86400),
            PendingHoldEntryUpdatedAtRangeRule(16, 32),
            PendingHoldEntryQuantityRangeRule(120, 86400),
        ),
    )
}
