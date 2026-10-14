package com.acme.transport.safety.nearmiss.missestimate

/** A rule a weighted near miss quota must pass. Check returns its problems, or none. */
interface WeightedNearMissQuotaRule {
    fun check(item: WeightedNearMissQuota): MutableList<String>
}

data class WeightedNearMissQuotaRequiredTimezoneRule(var message: String) : WeightedNearMissQuotaRule {
    override fun check(item: WeightedNearMissQuota): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissQuotaNameLengthRule(var maxLength: Int) : WeightedNearMissQuotaRule {
    override fun check(item: WeightedNearMissQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissQuotaRequiredNameRule(var message: String) : WeightedNearMissQuotaRule {
    override fun check(item: WeightedNearMissQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedNearMissQuotaPinnedGuardRule(var limit: Int) : WeightedNearMissQuotaRule {
    override fun check(item: WeightedNearMissQuota): MutableList<String> {
        if (item.pinned && item.priority > limit) {
            return mutableListOf<String>("priority ${item.priority} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted near miss quota. */
data class WeightedNearMissQuotaValidator(var rules: MutableList<WeightedNearMissQuotaRule>) {
    fun validate(item: WeightedNearMissQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedNearMissQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedNearMissQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedNearMissQuotaValidator(): WeightedNearMissQuotaValidator {
    return WeightedNearMissQuotaValidator(
        mutableListOf<WeightedNearMissQuotaRule>(
            WeightedNearMissQuotaRequiredTimezoneRule("weighted near miss quota needs an entry"),
            WeightedNearMissQuotaNameLengthRule(16),
            WeightedNearMissQuotaRequiredNameRule("weighted near miss quota needs an entry"),
            WeightedNearMissQuotaPinnedGuardRule(80),
        ),
    )
}
