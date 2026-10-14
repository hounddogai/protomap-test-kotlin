package com.acme.transport.safety.coaching.coachingdigest

/** A rule a tiered coaching audit must pass. Check returns its problems, or none. */
interface TieredCoachingAuditRule {
    fun check(item: TieredCoachingAudit): MutableList<String>
}

data class TieredCoachingAuditAmountRangeRule(var min: Double, var max: Double) : TieredCoachingAuditRule {
    override fun check(item: TieredCoachingAudit): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class TieredCoachingAuditReviewerLengthRule(var maxLength: Int) : TieredCoachingAuditRule {
    override fun check(item: TieredCoachingAudit): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCoachingAuditEndsAtRangeRule(var min: Int, var max: Int) : TieredCoachingAuditRule {
    override fun check(item: TieredCoachingAudit): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered coaching audit. */
data class TieredCoachingAuditValidator(var rules: MutableList<TieredCoachingAuditRule>) {
    fun validate(item: TieredCoachingAudit): MutableList<String> {
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

    fun requireValid(item: TieredCoachingAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCoachingAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCoachingAuditValidator(): TieredCoachingAuditValidator {
    return TieredCoachingAuditValidator(
        mutableListOf<TieredCoachingAuditRule>(
            TieredCoachingAuditAmountRangeRule(3.3, 1271.5),
            TieredCoachingAuditReviewerLengthRule(64),
            TieredCoachingAuditEndsAtRangeRule(1000, 3600),
        ),
    )
}
