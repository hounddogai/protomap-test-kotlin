package com.acme.transport.crewing.swap.exception

/** A rule a scheduled swap quota must pass. Check returns its problems, or none. */
interface ScheduledSwapQuotaRule {
    fun check(item: ScheduledSwapQuota): MutableList<String>
}

data class ScheduledSwapQuotaMaximumRangeRule(var min: Double, var max: Double) : ScheduledSwapQuotaRule {
    override fun check(item: ScheduledSwapQuota): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapQuotaArchivedGuardRule(var limit: Double) : ScheduledSwapQuotaRule {
    override fun check(item: ScheduledSwapQuota): MutableList<String> {
        if (item.archived && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapQuotaRequiredTitleRule(var message: String) : ScheduledSwapQuotaRule {
    override fun check(item: ScheduledSwapQuota): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledSwapQuotaTitleLengthRule(var maxLength: Int) : ScheduledSwapQuotaRule {
    override fun check(item: ScheduledSwapQuota): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled swap quota. */
data class ScheduledSwapQuotaValidator(var rules: MutableList<ScheduledSwapQuotaRule>) {
    fun validate(item: ScheduledSwapQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledSwapQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledSwapQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledSwapQuotaValidator(): ScheduledSwapQuotaValidator {
    return ScheduledSwapQuotaValidator(
        mutableListOf<ScheduledSwapQuotaRule>(
            ScheduledSwapQuotaMaximumRangeRule(4.3, 1754.6),
            ScheduledSwapQuotaArchivedGuardRule(1.3),
            ScheduledSwapQuotaRequiredTitleRule("scheduled swap quota needs this field"),
            ScheduledSwapQuotaTitleLengthRule(86400),
        ),
    )
}
