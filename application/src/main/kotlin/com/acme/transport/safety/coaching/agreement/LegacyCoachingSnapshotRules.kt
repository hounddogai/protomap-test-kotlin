package com.acme.transport.safety.coaching.agreement

/** A rule a legacy coaching snapshot must pass. Check returns its problems, or none. */
interface LegacyCoachingSnapshotRule {
    fun check(item: LegacyCoachingSnapshot): MutableList<String>
}

data class LegacyCoachingSnapshotRetriesRangeRule(var min: Int, var max: Int) : LegacyCoachingSnapshotRule {
    override fun check(item: LegacyCoachingSnapshot): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class LegacyCoachingSnapshotTitleLengthRule(var maxLength: Int) : LegacyCoachingSnapshotRule {
    override fun check(item: LegacyCoachingSnapshot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyCoachingSnapshotCodePrefixRule(var prefix: String) : LegacyCoachingSnapshotRule {
    override fun check(item: LegacyCoachingSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy coaching snapshot. */
data class LegacyCoachingSnapshotValidator(var rules: MutableList<LegacyCoachingSnapshotRule>) {
    fun validate(item: LegacyCoachingSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyCoachingSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyCoachingSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyCoachingSnapshotValidator(): LegacyCoachingSnapshotValidator {
    return LegacyCoachingSnapshotValidator(
        mutableListOf<LegacyCoachingSnapshotRule>(
            LegacyCoachingSnapshotRetriesRangeRule(80, 1000),
            LegacyCoachingSnapshotTitleLengthRule(16),
            LegacyCoachingSnapshotCodePrefixRule("REF"),
        ),
    )
}
