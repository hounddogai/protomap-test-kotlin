package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize

/** A rule a lease return profile must pass. Check returns its problems, or none. */
interface LeaseReturnProfileRule {
    fun check(item: LeaseReturnProfile): MutableList<String>
}

data class LeaseReturnProfileAllowedReviewerRule(var allowed: MutableList<String>) : LeaseReturnProfileRule {
    override fun check(item: LeaseReturnProfile): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is too long")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProfileCreatedAtRangeRule(var min: Int, var max: Int) : LeaseReturnProfileRule {
    override fun check(item: LeaseReturnProfile): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProfileCodeLengthRule(var maxLength: Int) : LeaseReturnProfileRule {
    override fun check(item: LeaseReturnProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProfileRequiredSourceRule(var message: String) : LeaseReturnProfileRule {
    override fun check(item: LeaseReturnProfile): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnProfileVisibleGuardRule(var limit: Int) : LeaseReturnProfileRule {
    override fun check(item: LeaseReturnProfile): MutableList<String> {
        if (item.visible && item.capacity > limit) {
            return mutableListOf<String>("capacity ${item.capacity} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return profile. */
data class LeaseReturnProfileValidator(var rules: MutableList<LeaseReturnProfileRule>) {
    fun validate(item: LeaseReturnProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LeaseReturnProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnProfileValidator(): LeaseReturnProfileValidator {
    return LeaseReturnProfileValidator(
        mutableListOf<LeaseReturnProfileRule>(
            LeaseReturnProfileAllowedReviewerRule(mutableListOf<String>("global", "scheduled", "default")),
            LeaseReturnProfileCreatedAtRangeRule(80, 86400),
            LeaseReturnProfileCodeLengthRule(86400),
            LeaseReturnProfileRequiredSourceRule("lease return profile needs a value"),
            LeaseReturnProfileVisibleGuardRule(16),
        ),
    )
}
