package com.acme.transport.fleetops.leasereturn.budget

/** A rule a lease return entry must pass. Check returns its problems, or none. */
interface LeaseReturnEntryRule {
    fun check(item: LeaseReturnEntry): MutableList<String>
}

data class LeaseReturnEntryDistanceKmRangeRule(var min: Double, var max: Double) : LeaseReturnEntryRule {
    override fun check(item: LeaseReturnEntry): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnEntryPriorityRangeRule(var min: Int, var max: Int) : LeaseReturnEntryRule {
    override fun check(item: LeaseReturnEntry): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class LeaseReturnEntryChannelLengthRule(var maxLength: Int) : LeaseReturnEntryRule {
    override fun check(item: LeaseReturnEntry): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnEntryTitleLengthRule(var maxLength: Int) : LeaseReturnEntryRule {
    override fun check(item: LeaseReturnEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnEntryRequiredCodeRule(var message: String) : LeaseReturnEntryRule {
    override fun check(item: LeaseReturnEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnEntryCodePrefixRule(var prefix: String) : LeaseReturnEntryRule {
    override fun check(item: LeaseReturnEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return entry. */
data class LeaseReturnEntryValidator(var rules: MutableList<LeaseReturnEntryRule>) {
    fun validate(item: LeaseReturnEntry): MutableList<String> {
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

    fun requireValid(item: LeaseReturnEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnEntryValidator(): LeaseReturnEntryValidator {
    return LeaseReturnEntryValidator(
        mutableListOf<LeaseReturnEntryRule>(
            LeaseReturnEntryDistanceKmRangeRule(5.6, 1380.9),
            LeaseReturnEntryPriorityRangeRule(16, 80),
            LeaseReturnEntryChannelLengthRule(64),
            LeaseReturnEntryTitleLengthRule(64),
            LeaseReturnEntryRequiredCodeRule("lease return entry needs a value"),
            LeaseReturnEntryCodePrefixRule("Q"),
        ),
    )
}
