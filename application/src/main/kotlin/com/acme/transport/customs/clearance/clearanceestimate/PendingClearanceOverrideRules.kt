package com.acme.transport.customs.clearance.clearanceestimate

/** A rule a pending clearance override must pass. Check returns its problems, or none. */
interface PendingClearanceOverrideRule {
    fun check(item: PendingClearanceOverride): MutableList<String>
}

data class PendingClearanceOverrideLaneLengthRule(var maxLength: Int) : PendingClearanceOverrideRule {
    override fun check(item: PendingClearanceOverride): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceOverrideRequiredCodeRule(var message: String) : PendingClearanceOverrideRule {
    override fun check(item: PendingClearanceOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceOverrideCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PendingClearanceOverrideRule {
    override fun check(item: PendingClearanceOverride): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingClearanceOverrideTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PendingClearanceOverrideRule {
    override fun check(item: PendingClearanceOverride): MutableList<String> {
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

data class PendingClearanceOverrideActiveGuardRule(var limit: Int) : PendingClearanceOverrideRule {
    override fun check(item: PendingClearanceOverride): MutableList<String> {
        if (item.active && item.etaMinutes > limit) {
            return mutableListOf<String>("eta minutes ${item.etaMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending clearance override. */
data class PendingClearanceOverrideValidator(var rules: MutableList<PendingClearanceOverrideRule>) {
    fun validate(item: PendingClearanceOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingClearanceOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingClearanceOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingClearanceOverrideValidator(): PendingClearanceOverrideValidator {
    return PendingClearanceOverrideValidator(
        mutableListOf<PendingClearanceOverrideRule>(
            PendingClearanceOverrideLaneLengthRule(1000),
            PendingClearanceOverrideRequiredCodeRule("pending clearance override needs an entry"),
            PendingClearanceOverrideCapacityRangeRule(32, 1000),
            PendingClearanceOverrideTagsLimitRule(1000, mutableListOf<String>("default")),
            PendingClearanceOverrideActiveGuardRule(32),
        ),
    )
}
