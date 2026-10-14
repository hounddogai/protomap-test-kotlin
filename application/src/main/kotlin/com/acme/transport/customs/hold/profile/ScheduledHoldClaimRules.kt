package com.acme.transport.customs.hold.profile

/** A rule a scheduled hold claim must pass. Check returns its problems, or none. */
interface ScheduledHoldClaimRule {
    fun check(item: ScheduledHoldClaim): MutableList<String>
}

data class ScheduledHoldClaimRequiredVehicleIdRule(var message: String) : ScheduledHoldClaimRule {
    override fun check(item: ScheduledHoldClaim): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldClaimExpiresAtRangeRule(var min: Int, var max: Int) : ScheduledHoldClaimRule {
    override fun check(item: ScheduledHoldClaim): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldClaimNameLengthRule(var maxLength: Int) : ScheduledHoldClaimRule {
    override fun check(item: ScheduledHoldClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldClaimRequiredCodeRule(var message: String) : ScheduledHoldClaimRule {
    override fun check(item: ScheduledHoldClaim): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldClaimRequiredSourceRule(var message: String) : ScheduledHoldClaimRule {
    override fun check(item: ScheduledHoldClaim): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledHoldClaimTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ScheduledHoldClaimRule {
    override fun check(item: ScheduledHoldClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

/** Runs every rule of a scheduled hold claim. */
data class ScheduledHoldClaimValidator(var rules: MutableList<ScheduledHoldClaimRule>) {
    fun validate(item: ScheduledHoldClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledHoldClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledHoldClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledHoldClaimValidator(): ScheduledHoldClaimValidator {
    return ScheduledHoldClaimValidator(
        mutableListOf<ScheduledHoldClaimRule>(
            ScheduledHoldClaimRequiredVehicleIdRule("scheduled hold claim needs this field"),
            ScheduledHoldClaimExpiresAtRangeRule(64, 64),
            ScheduledHoldClaimNameLengthRule(120),
            ScheduledHoldClaimRequiredCodeRule("scheduled hold claim needs a value"),
            ScheduledHoldClaimRequiredSourceRule("scheduled hold claim needs a value"),
            ScheduledHoldClaimTagsLimitRule(255, mutableListOf<String>("regional", "strict", "rolling")),
        ),
    )
}
