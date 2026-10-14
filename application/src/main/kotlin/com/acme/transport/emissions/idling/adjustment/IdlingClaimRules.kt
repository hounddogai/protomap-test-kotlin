package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a idling claim must pass. Check returns its problems, or none. */
interface IdlingClaimRule {
    fun check(item: IdlingClaim): MutableList<String>
}

data class IdlingClaimRequiredCodeRule(var message: String) : IdlingClaimRule {
    override fun check(item: IdlingClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingClaimDurationMinutesRangeRule(var min: Int, var max: Int) : IdlingClaimRule {
    override fun check(item: IdlingClaim): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingClaimAllowedPhoneRule(var allowed: MutableList<String>) : IdlingClaimRule {
    override fun check(item: IdlingClaim): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.phone))) {
            return mutableListOf<String>("phone ${item.phone} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class IdlingClaimOwnerIdLengthRule(var maxLength: Int) : IdlingClaimRule {
    override fun check(item: IdlingClaim): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingClaimRequiredOwnerIdRule(var message: String) : IdlingClaimRule {
    override fun check(item: IdlingClaim): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling claim. */
data class IdlingClaimValidator(var rules: MutableList<IdlingClaimRule>) {
    fun validate(item: IdlingClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingClaimValidator(): IdlingClaimValidator {
    return IdlingClaimValidator(
        mutableListOf<IdlingClaimRule>(
            IdlingClaimRequiredCodeRule("idling claim needs an entry"),
            IdlingClaimDurationMinutesRangeRule(1000, 86400),
            IdlingClaimAllowedPhoneRule(mutableListOf<String>("lenient", "weighted")),
            IdlingClaimOwnerIdLengthRule(64),
            IdlingClaimRequiredOwnerIdRule("idling claim needs an entry"),
        ),
    )
}
