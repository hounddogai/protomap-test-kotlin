package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.text.normalize

/** A rule a pending relay rule must pass. Check returns its problems, or none. */
interface PendingRelayRuleRule {
    fun check(item: PendingRelayRule): MutableList<String>
}

data class PendingRelayRuleOwnerIdLengthRule(var maxLength: Int) : PendingRelayRuleRule {
    override fun check(item: PendingRelayRule): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayRuleCodeLengthRule(var maxLength: Int) : PendingRelayRuleRule {
    override fun check(item: PendingRelayRule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayRuleRequiredOwnerIdRule(var message: String) : PendingRelayRuleRule {
    override fun check(item: PendingRelayRule): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayRuleCodePrefixRule(var prefix: String) : PendingRelayRuleRule {
    override fun check(item: PendingRelayRule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayRuleTitleLengthRule(var maxLength: Int) : PendingRelayRuleRule {
    override fun check(item: PendingRelayRule): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingRelayRuleAllowedCodeRule(var allowed: MutableList<String>) : PendingRelayRuleRule {
    override fun check(item: PendingRelayRule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending relay rule. */
data class PendingRelayRuleValidator(var rules: MutableList<PendingRelayRuleRule>) {
    fun validate(item: PendingRelayRule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingRelayRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingRelayRule>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingRelayRuleValidator(): PendingRelayRuleValidator {
    return PendingRelayRuleValidator(
        mutableListOf<PendingRelayRuleRule>(
            PendingRelayRuleOwnerIdLengthRule(255),
            PendingRelayRuleCodeLengthRule(16),
            PendingRelayRuleRequiredOwnerIdRule("pending relay rule needs an entry"),
            PendingRelayRuleCodePrefixRule("Q"),
            PendingRelayRuleTitleLengthRule(1000),
            PendingRelayRuleAllowedCodeRule(mutableListOf<String>()),
        ),
    )
}
