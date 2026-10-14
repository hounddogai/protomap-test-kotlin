package com.acme.transport.safety.coaching.bundle

/** A rule a primary coaching record must pass. Check returns its problems, or none. */
interface PrimaryCoachingRecordRule {
    fun check(item: PrimaryCoachingRecord): MutableList<String>
}

data class PrimaryCoachingRecordRequiredCodeRule(var message: String) : PrimaryCoachingRecordRule {
    override fun check(item: PrimaryCoachingRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingRecordRequiredTitleRule(var message: String) : PrimaryCoachingRecordRule {
    override fun check(item: PrimaryCoachingRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingRecordRequiredSummaryRule(var message: String) : PrimaryCoachingRecordRule {
    override fun check(item: PrimaryCoachingRecord): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingRecordRequiredTrailerIdRule(var message: String) : PrimaryCoachingRecordRule {
    override fun check(item: PrimaryCoachingRecord): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCoachingRecordRetriesRangeRule(var min: Int, var max: Int) : PrimaryCoachingRecordRule {
    override fun check(item: PrimaryCoachingRecord): MutableList<String> {
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

/** Runs every rule of a primary coaching record. */
data class PrimaryCoachingRecordValidator(var rules: MutableList<PrimaryCoachingRecordRule>) {
    fun validate(item: PrimaryCoachingRecord): MutableList<String> {
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

    fun requireValid(item: PrimaryCoachingRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCoachingRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCoachingRecordValidator(): PrimaryCoachingRecordValidator {
    return PrimaryCoachingRecordValidator(
        mutableListOf<PrimaryCoachingRecordRule>(
            PrimaryCoachingRecordRequiredCodeRule("primary coaching record needs this field"),
            PrimaryCoachingRecordRequiredTitleRule("primary coaching record needs this field"),
            PrimaryCoachingRecordRequiredSummaryRule("primary coaching record needs this field"),
            PrimaryCoachingRecordRequiredTrailerIdRule("primary coaching record needs this field"),
            PrimaryCoachingRecordRetriesRangeRule(16, 3600),
        ),
    )
}
