package com.acme.transport.fleetops.leasereturn.bundle

/** A rule a lease return override must pass. Check returns its problems, or none. */
interface LeaseReturnOverrideRule {
    fun check(item: LeaseReturnOverride): MutableList<String>
}

data class LeaseReturnOverrideRequiredTitleRule(var message: String) : LeaseReturnOverrideRule {
    override fun check(item: LeaseReturnOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnOverrideCapacityRangeRule(var min: Int, var max: Int) : LeaseReturnOverrideRule {
    override fun check(item: LeaseReturnOverride): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnOverrideGraceDaysRangeRule(var min: Int, var max: Int) : LeaseReturnOverrideRule {
    override fun check(item: LeaseReturnOverride): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnOverrideRateRangeRule(var min: Double, var max: Double) : LeaseReturnOverrideRule {
    override fun check(item: LeaseReturnOverride): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnOverridePinnedGuardRule(var limit: Int) : LeaseReturnOverrideRule {
    override fun check(item: LeaseReturnOverride): MutableList<String> {
        if (item.pinned && item.graceDays > limit) {
            return mutableListOf<String>("grace days ${item.graceDays} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnOverrideRequiredSegmentRule(var message: String) : LeaseReturnOverrideRule {
    override fun check(item: LeaseReturnOverride): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return override. */
data class LeaseReturnOverrideValidator(var rules: MutableList<LeaseReturnOverrideRule>) {
    fun validate(item: LeaseReturnOverride): MutableList<String> {
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

    fun requireValid(item: LeaseReturnOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnOverrideValidator(): LeaseReturnOverrideValidator {
    return LeaseReturnOverrideValidator(
        mutableListOf<LeaseReturnOverrideRule>(
            LeaseReturnOverrideRequiredTitleRule("lease return override needs a value"),
            LeaseReturnOverrideCapacityRangeRule(64, 255),
            LeaseReturnOverrideGraceDaysRangeRule(32, 1000),
            LeaseReturnOverrideRateRangeRule(7.0, 4579.8),
            LeaseReturnOverridePinnedGuardRule(86400),
            LeaseReturnOverrideRequiredSegmentRule("lease return override needs a value"),
        ),
    )
}
