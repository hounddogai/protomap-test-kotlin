package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.text.normalize

/** A rule a scheduled locker claim must pass. Check returns its problems, or none. */
interface ScheduledLockerClaimRule {
    fun check(item: ScheduledLockerClaim): MutableList<String>
}

data class ScheduledLockerClaimAllowedNameRule(var allowed: MutableList<String>) : ScheduledLockerClaimRule {
    override fun check(item: ScheduledLockerClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerClaimReviewerLengthRule(var maxLength: Int) : ScheduledLockerClaimRule {
    override fun check(item: ScheduledLockerClaim): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerClaimRequiredCodeRule(var message: String) : ScheduledLockerClaimRule {
    override fun check(item: ScheduledLockerClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerClaimNameLengthRule(var maxLength: Int) : ScheduledLockerClaimRule {
    override fun check(item: ScheduledLockerClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerClaimCodePrefixRule(var prefix: String) : ScheduledLockerClaimRule {
    override fun check(item: ScheduledLockerClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled locker claim. */
data class ScheduledLockerClaimValidator(var rules: MutableList<ScheduledLockerClaimRule>) {
    fun validate(item: ScheduledLockerClaim): MutableList<String> {
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

    fun requireValid(item: ScheduledLockerClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLockerClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLockerClaimValidator(): ScheduledLockerClaimValidator {
    return ScheduledLockerClaimValidator(
        mutableListOf<ScheduledLockerClaimRule>(
            ScheduledLockerClaimAllowedNameRule(mutableListOf<String>("tiered")),
            ScheduledLockerClaimReviewerLengthRule(32),
            ScheduledLockerClaimRequiredCodeRule("scheduled locker claim needs an entry"),
            ScheduledLockerClaimNameLengthRule(16),
            ScheduledLockerClaimCodePrefixRule("OPS"),
        ),
    )
}
