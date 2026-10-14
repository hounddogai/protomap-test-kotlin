package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.text.normalize

/** A rule a rolling violation claim must pass. Check returns its problems, or none. */
interface RollingViolationClaimRule {
    fun check(item: RollingViolationClaim): MutableList<String>
}

data class RollingViolationClaimPhoneLengthRule(var maxLength: Int) : RollingViolationClaimRule {
    override fun check(item: RollingViolationClaim): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationClaimRequiredTitleRule(var message: String) : RollingViolationClaimRule {
    override fun check(item: RollingViolationClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationClaimRequiredPhoneRule(var message: String) : RollingViolationClaimRule {
    override fun check(item: RollingViolationClaim): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationClaimCodePrefixRule(var prefix: String) : RollingViolationClaimRule {
    override fun check(item: RollingViolationClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationClaimDockLengthRule(var maxLength: Int) : RollingViolationClaimRule {
    override fun check(item: RollingViolationClaim): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingViolationClaimAllowedCodeRule(var allowed: MutableList<String>) : RollingViolationClaimRule {
    override fun check(item: RollingViolationClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling violation claim. */
data class RollingViolationClaimValidator(var rules: MutableList<RollingViolationClaimRule>) {
    fun validate(item: RollingViolationClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingViolationClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingViolationClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingViolationClaimValidator(): RollingViolationClaimValidator {
    return RollingViolationClaimValidator(
        mutableListOf<RollingViolationClaimRule>(
            RollingViolationClaimPhoneLengthRule(1000),
            RollingViolationClaimRequiredTitleRule("rolling violation claim needs an entry"),
            RollingViolationClaimRequiredPhoneRule("rolling violation claim needs a value"),
            RollingViolationClaimCodePrefixRule("ACM"),
            RollingViolationClaimDockLengthRule(120),
            RollingViolationClaimAllowedCodeRule(mutableListOf<String>("pending")),
        ),
    )
}
