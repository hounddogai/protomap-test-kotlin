package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize

/** A rule a pending locker claim must pass. Check returns its problems, or none. */
interface PendingLockerClaimRule {
    fun check(item: PendingLockerClaim): MutableList<String>
}

data class PendingLockerClaimTitleLengthRule(var maxLength: Int) : PendingLockerClaimRule {
    override fun check(item: PendingLockerClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerClaimCarrierLengthRule(var maxLength: Int) : PendingLockerClaimRule {
    override fun check(item: PendingLockerClaim): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerClaimCodeLengthRule(var maxLength: Int) : PendingLockerClaimRule {
    override fun check(item: PendingLockerClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerClaimRequiredTitleRule(var message: String) : PendingLockerClaimRule {
    override fun check(item: PendingLockerClaim): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerClaimAllowedCodeRule(var allowed: MutableList<String>) : PendingLockerClaimRule {
    override fun check(item: PendingLockerClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class PendingLockerClaimCodePrefixRule(var prefix: String) : PendingLockerClaimRule {
    override fun check(item: PendingLockerClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending locker claim. */
data class PendingLockerClaimValidator(var rules: MutableList<PendingLockerClaimRule>) {
    fun validate(item: PendingLockerClaim): MutableList<String> {
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

    fun requireValid(item: PendingLockerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLockerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLockerClaimValidator(): PendingLockerClaimValidator {
    return PendingLockerClaimValidator(
        mutableListOf<PendingLockerClaimRule>(
            PendingLockerClaimTitleLengthRule(80),
            PendingLockerClaimCarrierLengthRule(255),
            PendingLockerClaimCodeLengthRule(120),
            PendingLockerClaimRequiredTitleRule("pending locker claim needs an entry"),
            PendingLockerClaimAllowedCodeRule(mutableListOf<String>("regional")),
            PendingLockerClaimCodePrefixRule("REF"),
        ),
    )
}
