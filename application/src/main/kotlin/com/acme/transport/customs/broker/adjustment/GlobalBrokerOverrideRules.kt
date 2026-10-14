package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a global broker override must pass. Check returns its problems, or none. */
interface GlobalBrokerOverrideRule {
    fun check(item: GlobalBrokerOverride): MutableList<String>
}

data class GlobalBrokerOverrideRequiredLocaleRule(var message: String) : GlobalBrokerOverrideRule {
    override fun check(item: GlobalBrokerOverride): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerOverrideCodeLengthRule(var maxLength: Int) : GlobalBrokerOverrideRule {
    override fun check(item: GlobalBrokerOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerOverrideAllowedSourceRule(var allowed: MutableList<String>) : GlobalBrokerOverrideRule {
    override fun check(item: GlobalBrokerOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is expired")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerOverrideLocaleLengthRule(var maxLength: Int) : GlobalBrokerOverrideRule {
    override fun check(item: GlobalBrokerOverride): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global broker override. */
data class GlobalBrokerOverrideValidator(var rules: MutableList<GlobalBrokerOverrideRule>) {
    fun validate(item: GlobalBrokerOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalBrokerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBrokerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBrokerOverrideValidator(): GlobalBrokerOverrideValidator {
    return GlobalBrokerOverrideValidator(
        mutableListOf<GlobalBrokerOverrideRule>(
            GlobalBrokerOverrideRequiredLocaleRule("global broker override needs a value"),
            GlobalBrokerOverrideCodeLengthRule(64),
            GlobalBrokerOverrideAllowedSourceRule(mutableListOf<String>()),
            GlobalBrokerOverrideLocaleLengthRule(64),
        ),
    )
}
