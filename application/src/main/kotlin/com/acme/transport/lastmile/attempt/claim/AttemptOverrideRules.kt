package com.acme.transport.lastmile.attempt.claim

/** A rule a attempt override must pass. Check returns its problems, or none. */
interface AttemptOverrideRule {
    fun check(item: AttemptOverride): MutableList<String>
}

data class AttemptOverrideDistanceKmRangeRule(var min: Double, var max: Double) : AttemptOverrideRule {
    override fun check(item: AttemptOverride): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptOverrideRequiredCodeRule(var message: String) : AttemptOverrideRule {
    override fun check(item: AttemptOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AttemptOverrideTitleLengthRule(var maxLength: Int) : AttemptOverrideRule {
    override fun check(item: AttemptOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptOverridePinnedGuardRule(var limit: Int) : AttemptOverrideRule {
    override fun check(item: AttemptOverride): MutableList<String> {
        if (item.pinned && item.limit > limit) {
            return mutableListOf<String>("limit ${item.limit} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class AttemptOverrideRequiredTitleRule(var message: String) : AttemptOverrideRule {
    override fun check(item: AttemptOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt override. */
data class AttemptOverrideValidator(var rules: MutableList<AttemptOverrideRule>) {
    fun validate(item: AttemptOverride): MutableList<String> {
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

    fun requireValid(item: AttemptOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptOverrideValidator(): AttemptOverrideValidator {
    return AttemptOverrideValidator(
        mutableListOf<AttemptOverrideRule>(
            AttemptOverrideDistanceKmRangeRule(8.2, 1002.3),
            AttemptOverrideRequiredCodeRule("attempt override needs this field"),
            AttemptOverrideTitleLengthRule(1000),
            AttemptOverridePinnedGuardRule(16),
            AttemptOverrideRequiredTitleRule("attempt override needs a value"),
        ),
    )
}
