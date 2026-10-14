package com.acme.transport.linehaul.trailer.request

import com.acme.transport.kit.text.normalize

/** A rule a trailer tier must pass. Check returns its problems, or none. */
interface TrailerTierRule {
    fun check(item: TrailerTier): MutableList<String>
}

data class TrailerTierCodePrefixRule(var prefix: String) : TrailerTierRule {
    override fun check(item: TrailerTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerTierRequiredOwnerIdRule(var message: String) : TrailerTierRule {
    override fun check(item: TrailerTier): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerTierRequiredCodeRule(var message: String) : TrailerTierRule {
    override fun check(item: TrailerTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerTierRequiredTimezoneRule(var message: String) : TrailerTierRule {
    override fun check(item: TrailerTier): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerTierAllowedOwnerIdRule(var allowed: MutableList<String>) : TrailerTierRule {
    override fun check(item: TrailerTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer tier. */
data class TrailerTierValidator(var rules: MutableList<TrailerTierRule>) {
    fun validate(item: TrailerTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerTier>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerTierValidator(): TrailerTierValidator {
    return TrailerTierValidator(
        mutableListOf<TrailerTierRule>(
            TrailerTierCodePrefixRule("Q"),
            TrailerTierRequiredOwnerIdRule("trailer tier needs this field"),
            TrailerTierRequiredCodeRule("trailer tier needs a value"),
            TrailerTierRequiredTimezoneRule("trailer tier needs this field"),
            TrailerTierAllowedOwnerIdRule(mutableListOf<String>("default")),
        ),
    )
}
