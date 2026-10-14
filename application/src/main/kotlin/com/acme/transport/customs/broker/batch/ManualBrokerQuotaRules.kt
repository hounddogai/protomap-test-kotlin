package com.acme.transport.customs.broker.batch

/** A rule a manual broker quota must pass. Check returns its problems, or none. */
interface ManualBrokerQuotaRule {
    fun check(item: ManualBrokerQuota): MutableList<String>
}

data class ManualBrokerQuotaTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ManualBrokerQuotaRule {
    override fun check(item: ManualBrokerQuota): MutableList<String> {
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

data class ManualBrokerQuotaNameLengthRule(var maxLength: Int) : ManualBrokerQuotaRule {
    override fun check(item: ManualBrokerQuota): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerQuotaRequiredNameRule(var message: String) : ManualBrokerQuotaRule {
    override fun check(item: ManualBrokerQuota): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerQuotaApprovedGuardRule(var limit: Int) : ManualBrokerQuotaRule {
    override fun check(item: ManualBrokerQuota): MutableList<String> {
        if (item.approved && item.batchSize > limit) {
            return mutableListOf<String>("batch size ${item.batchSize} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerQuotaWindowDaysRangeRule(var min: Int, var max: Int) : ManualBrokerQuotaRule {
    override fun check(item: ManualBrokerQuota): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual broker quota. */
data class ManualBrokerQuotaValidator(var rules: MutableList<ManualBrokerQuotaRule>) {
    fun validate(item: ManualBrokerQuota): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualBrokerQuota) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBrokerQuota>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBrokerQuotaValidator(): ManualBrokerQuotaValidator {
    return ManualBrokerQuotaValidator(
        mutableListOf<ManualBrokerQuotaRule>(
            ManualBrokerQuotaTagsLimitRule(255, mutableListOf<String>("default", "fallback", "capped")),
            ManualBrokerQuotaNameLengthRule(80),
            ManualBrokerQuotaRequiredNameRule("manual broker quota needs this field"),
            ManualBrokerQuotaApprovedGuardRule(1000),
            ManualBrokerQuotaWindowDaysRangeRule(32, 64),
        ),
    )
}
