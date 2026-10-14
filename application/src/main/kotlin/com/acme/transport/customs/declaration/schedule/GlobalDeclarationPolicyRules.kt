package com.acme.transport.customs.declaration.schedule

import com.acme.transport.kit.text.normalize

/** A rule a global declaration policy must pass. Check returns its problems, or none. */
interface GlobalDeclarationPolicyRule {
    fun check(item: GlobalDeclarationPolicy): MutableList<String>
}

data class GlobalDeclarationPolicyApprovedGuardRule(var limit: Int) : GlobalDeclarationPolicyRule {
    override fun check(item: GlobalDeclarationPolicy): MutableList<String> {
        if (item.approved && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationPolicyCodeLengthRule(var maxLength: Int) : GlobalDeclarationPolicyRule {
    override fun check(item: GlobalDeclarationPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationPolicyDockLengthRule(var maxLength: Int) : GlobalDeclarationPolicyRule {
    override fun check(item: GlobalDeclarationPolicy): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationPolicyAllowedCodeRule(
    var allowed: MutableList<String>,
) : GlobalDeclarationPolicyRule {
    override fun check(item: GlobalDeclarationPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationPolicyRequiredExternalRefRule(var message: String) : GlobalDeclarationPolicyRule {
    override fun check(item: GlobalDeclarationPolicy): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalDeclarationPolicyRequiredCodeRule(var message: String) : GlobalDeclarationPolicyRule {
    override fun check(item: GlobalDeclarationPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global declaration policy. */
data class GlobalDeclarationPolicyValidator(var rules: MutableList<GlobalDeclarationPolicyRule>) {
    fun validate(item: GlobalDeclarationPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalDeclarationPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalDeclarationPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalDeclarationPolicyValidator(): GlobalDeclarationPolicyValidator {
    return GlobalDeclarationPolicyValidator(
        mutableListOf<GlobalDeclarationPolicyRule>(
            GlobalDeclarationPolicyApprovedGuardRule(86400),
            GlobalDeclarationPolicyCodeLengthRule(86400),
            GlobalDeclarationPolicyDockLengthRule(3600),
            GlobalDeclarationPolicyAllowedCodeRule(mutableListOf<String>("pending")),
            GlobalDeclarationPolicyRequiredExternalRefRule("global declaration policy needs a value"),
            GlobalDeclarationPolicyRequiredCodeRule("global declaration policy needs an entry"),
        ),
    )
}
