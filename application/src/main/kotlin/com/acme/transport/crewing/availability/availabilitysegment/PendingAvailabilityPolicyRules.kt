package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.text.normalize

/** A rule a pending availability policy must pass. Check returns its problems, or none. */
interface PendingAvailabilityPolicyRule {
    fun check(item: PendingAvailabilityPolicy): MutableList<String>
}

data class PendingAvailabilityPolicyRequiredLabelRule(var message: String) : PendingAvailabilityPolicyRule {
    override fun check(item: PendingAvailabilityPolicy): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityPolicyActiveGuardRule(var limit: Double) : PendingAvailabilityPolicyRule {
    override fun check(item: PendingAvailabilityPolicy): MutableList<String> {
        if (item.active && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityPolicyCodeLengthRule(var maxLength: Int) : PendingAvailabilityPolicyRule {
    override fun check(item: PendingAvailabilityPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityPolicyRequiredCodeRule(var message: String) : PendingAvailabilityPolicyRule {
    override fun check(item: PendingAvailabilityPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityPolicyAllowedLabelRule(
    var allowed: MutableList<String>,
) : PendingAvailabilityPolicyRule {
    override fun check(item: PendingAvailabilityPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending availability policy. */
data class PendingAvailabilityPolicyValidator(var rules: MutableList<PendingAvailabilityPolicyRule>) {
    fun validate(item: PendingAvailabilityPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAvailabilityPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAvailabilityPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAvailabilityPolicyValidator(): PendingAvailabilityPolicyValidator {
    return PendingAvailabilityPolicyValidator(
        mutableListOf<PendingAvailabilityPolicyRule>(
            PendingAvailabilityPolicyRequiredLabelRule("pending availability policy needs a value"),
            PendingAvailabilityPolicyActiveGuardRule(3.9),
            PendingAvailabilityPolicyCodeLengthRule(86400),
            PendingAvailabilityPolicyRequiredCodeRule("pending availability policy needs an entry"),
            PendingAvailabilityPolicyAllowedLabelRule(mutableListOf<String>("adaptive", "lenient")),
        ),
    )
}
