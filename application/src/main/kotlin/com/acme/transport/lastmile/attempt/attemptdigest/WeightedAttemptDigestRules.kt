package com.acme.transport.lastmile.attempt.attemptdigest

/** A rule a weighted attempt digest must pass. Check returns its problems, or none. */
interface WeightedAttemptDigestRule {
    fun check(item: WeightedAttemptDigest): MutableList<String>
}

data class WeightedAttemptDigestRequiredNotesRule(var message: String) : WeightedAttemptDigestRule {
    override fun check(item: WeightedAttemptDigest): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAttemptDigestRequiredTitleRule(var message: String) : WeightedAttemptDigestRule {
    override fun check(item: WeightedAttemptDigest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAttemptDigestStartsAtRangeRule(var min: Int, var max: Int) : WeightedAttemptDigestRule {
    override fun check(item: WeightedAttemptDigest): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAttemptDigestActiveGuardRule(var limit: Double) : WeightedAttemptDigestRule {
    override fun check(item: WeightedAttemptDigest): MutableList<String> {
        if (item.active && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted attempt digest. */
data class WeightedAttemptDigestValidator(var rules: MutableList<WeightedAttemptDigestRule>) {
    fun validate(item: WeightedAttemptDigest): MutableList<String> {
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

    fun requireValid(item: WeightedAttemptDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAttemptDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAttemptDigestValidator(): WeightedAttemptDigestValidator {
    return WeightedAttemptDigestValidator(
        mutableListOf<WeightedAttemptDigestRule>(
            WeightedAttemptDigestRequiredNotesRule("weighted attempt digest needs a value"),
            WeightedAttemptDigestRequiredTitleRule("weighted attempt digest needs an entry"),
            WeightedAttemptDigestStartsAtRangeRule(16, 86400),
            WeightedAttemptDigestActiveGuardRule(8.3),
        ),
    )
}
