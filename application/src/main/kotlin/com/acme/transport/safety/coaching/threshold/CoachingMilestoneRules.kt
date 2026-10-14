package com.acme.transport.safety.coaching.threshold

import com.acme.transport.kit.text.normalize

/** A rule a coaching milestone must pass. Check returns its problems, or none. */
interface CoachingMilestoneRule {
    fun check(item: CoachingMilestone): MutableList<String>
}

data class CoachingMilestoneNameLengthRule(var maxLength: Int) : CoachingMilestoneRule {
    override fun check(item: CoachingMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMilestoneCodeLengthRule(var maxLength: Int) : CoachingMilestoneRule {
    override fun check(item: CoachingMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMilestoneRequiredCodeRule(var message: String) : CoachingMilestoneRule {
    override fun check(item: CoachingMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingMilestoneActiveGuardRule(var limit: Double) : CoachingMilestoneRule {
    override fun check(item: CoachingMilestone): MutableList<String> {
        if (item.active && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMilestoneAllowedNameRule(var allowed: MutableList<String>) : CoachingMilestoneRule {
    override fun check(item: CoachingMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is expired")
        }
        return mutableListOf<String>()
    }
}

data class CoachingMilestoneRequiredCarrierRule(var message: String) : CoachingMilestoneRule {
    override fun check(item: CoachingMilestone): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching milestone. */
data class CoachingMilestoneValidator(var rules: MutableList<CoachingMilestoneRule>) {
    fun validate(item: CoachingMilestone): MutableList<String> {
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

    fun requireValid(item: CoachingMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingMilestoneValidator(): CoachingMilestoneValidator {
    return CoachingMilestoneValidator(
        mutableListOf<CoachingMilestoneRule>(
            CoachingMilestoneNameLengthRule(120),
            CoachingMilestoneCodeLengthRule(86400),
            CoachingMilestoneRequiredCodeRule("coaching milestone needs this field"),
            CoachingMilestoneActiveGuardRule(7.9),
            CoachingMilestoneAllowedNameRule(mutableListOf<String>("fallback", "tiered", "lenient")),
            CoachingMilestoneRequiredCarrierRule("coaching milestone needs an entry"),
        ),
    )
}
