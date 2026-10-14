package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.text.normalize

/** A rule a rolling coaching tier must pass. Check returns its problems, or none. */
interface RollingCoachingTierRule {
    fun check(item: RollingCoachingTier): MutableList<String>
}

data class RollingCoachingTierRequiredNameRule(var message: String) : RollingCoachingTierRule {
    override fun check(item: RollingCoachingTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingTierLaneLengthRule(var maxLength: Int) : RollingCoachingTierRule {
    override fun check(item: RollingCoachingTier): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingTierAllowedLaneRule(var allowed: MutableList<String>) : RollingCoachingTierRule {
    override fun check(item: RollingCoachingTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is expired")
        }
        return mutableListOf<String>()
    }
}

data class RollingCoachingTierRequiredCurrencyRule(var message: String) : RollingCoachingTierRule {
    override fun check(item: RollingCoachingTier): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling coaching tier. */
data class RollingCoachingTierValidator(var rules: MutableList<RollingCoachingTierRule>) {
    fun validate(item: RollingCoachingTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingCoachingTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCoachingTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCoachingTierValidator(): RollingCoachingTierValidator {
    return RollingCoachingTierValidator(
        mutableListOf<RollingCoachingTierRule>(
            RollingCoachingTierRequiredNameRule("rolling coaching tier needs a value"),
            RollingCoachingTierLaneLengthRule(64),
            RollingCoachingTierAllowedLaneRule(mutableListOf<String>("capped", "seasonal", "lenient")),
            RollingCoachingTierRequiredCurrencyRule("rolling coaching tier needs a value"),
        ),
    )
}
