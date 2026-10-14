package com.acme.transport.crewing.availability.availabilitytier

/** A rule a pending availability tier must pass. Check returns its problems, or none. */
interface PendingAvailabilityTierRule {
    fun check(item: PendingAvailabilityTier): MutableList<String>
}

data class PendingAvailabilityTierApprovedGuardRule(var limit: Double) : PendingAvailabilityTierRule {
    override fun check(item: PendingAvailabilityTier): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityTierOwnerIdLengthRule(var maxLength: Int) : PendingAvailabilityTierRule {
    override fun check(item: PendingAvailabilityTier): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityTierCodePrefixRule(var prefix: String) : PendingAvailabilityTierRule {
    override fun check(item: PendingAvailabilityTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingAvailabilityTierRequiredLabelRule(var message: String) : PendingAvailabilityTierRule {
    override fun check(item: PendingAvailabilityTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending availability tier. */
data class PendingAvailabilityTierValidator(var rules: MutableList<PendingAvailabilityTierRule>) {
    fun validate(item: PendingAvailabilityTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAvailabilityTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAvailabilityTier>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAvailabilityTierValidator(): PendingAvailabilityTierValidator {
    return PendingAvailabilityTierValidator(
        mutableListOf<PendingAvailabilityTierRule>(
            PendingAvailabilityTierApprovedGuardRule(1.0),
            PendingAvailabilityTierOwnerIdLengthRule(255),
            PendingAvailabilityTierCodePrefixRule("REF"),
            PendingAvailabilityTierRequiredLabelRule("pending availability tier needs an entry"),
        ),
    )
}
