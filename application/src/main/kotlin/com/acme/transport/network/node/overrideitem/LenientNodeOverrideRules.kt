package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a lenient node override must pass. Check returns its problems, or none. */
interface LenientNodeOverrideRule {
    fun check(item: LenientNodeOverride): MutableList<String>
}

data class LenientNodeOverrideAllowedDriverIdRule(var allowed: MutableList<String>) : LenientNodeOverrideRule {
    override fun check(item: LenientNodeOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeOverrideRequiredNameRule(var message: String) : LenientNodeOverrideRule {
    override fun check(item: LenientNodeOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeOverrideRequiredCodeRule(var message: String) : LenientNodeOverrideRule {
    override fun check(item: LenientNodeOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeOverrideRequiredCurrencyRule(var message: String) : LenientNodeOverrideRule {
    override fun check(item: LenientNodeOverride): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeOverrideRequiredCategoryRule(var message: String) : LenientNodeOverrideRule {
    override fun check(item: LenientNodeOverride): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientNodeOverrideCodePrefixRule(var prefix: String) : LenientNodeOverrideRule {
    override fun check(item: LenientNodeOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient node override. */
data class LenientNodeOverrideValidator(var rules: MutableList<LenientNodeOverrideRule>) {
    fun validate(item: LenientNodeOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientNodeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientNodeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientNodeOverrideValidator(): LenientNodeOverrideValidator {
    return LenientNodeOverrideValidator(
        mutableListOf<LenientNodeOverrideRule>(
            LenientNodeOverrideAllowedDriverIdRule(mutableListOf<String>("weighted", "fallback", "weighted")),
            LenientNodeOverrideRequiredNameRule("lenient node override needs this field"),
            LenientNodeOverrideRequiredCodeRule("lenient node override needs this field"),
            LenientNodeOverrideRequiredCurrencyRule("lenient node override needs a value"),
            LenientNodeOverrideRequiredCategoryRule("lenient node override needs an entry"),
            LenientNodeOverrideCodePrefixRule("Q"),
        ),
    )
}
