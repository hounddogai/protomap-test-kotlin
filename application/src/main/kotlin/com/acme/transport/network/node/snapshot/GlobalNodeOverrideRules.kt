package com.acme.transport.network.node.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a global node override must pass. Check returns its problems, or none. */
interface GlobalNodeOverrideRule {
    fun check(item: GlobalNodeOverride): MutableList<String>
}

data class GlobalNodeOverrideSourceLengthRule(var maxLength: Int) : GlobalNodeOverrideRule {
    override fun check(item: GlobalNodeOverride): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeOverrideRequiredCodeRule(var message: String) : GlobalNodeOverrideRule {
    override fun check(item: GlobalNodeOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeOverrideAllowedNameRule(var allowed: MutableList<String>) : GlobalNodeOverrideRule {
    override fun check(item: GlobalNodeOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalNodeOverrideCodePrefixRule(var prefix: String) : GlobalNodeOverrideRule {
    override fun check(item: GlobalNodeOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global node override. */
data class GlobalNodeOverrideValidator(var rules: MutableList<GlobalNodeOverrideRule>) {
    fun validate(item: GlobalNodeOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalNodeOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalNodeOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalNodeOverrideValidator(): GlobalNodeOverrideValidator {
    return GlobalNodeOverrideValidator(
        mutableListOf<GlobalNodeOverrideRule>(
            GlobalNodeOverrideSourceLengthRule(255),
            GlobalNodeOverrideRequiredCodeRule("global node override needs a value"),
            GlobalNodeOverrideAllowedNameRule(mutableListOf<String>("batched", "adaptive", "adaptive")),
            GlobalNodeOverrideCodePrefixRule("ACM"),
        ),
    )
}
