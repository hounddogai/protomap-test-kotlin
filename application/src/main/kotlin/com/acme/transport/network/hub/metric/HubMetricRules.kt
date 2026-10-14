package com.acme.transport.network.hub.metric

/** A rule a hub metric must pass. Check returns its problems, or none. */
interface HubMetricRule {
    fun check(item: HubMetric): MutableList<String>
}

data class HubMetricApprovedGuardRule(var limit: Int) : HubMetricRule {
    override fun check(item: HubMetric): MutableList<String> {
        if (item.approved && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class HubMetricTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : HubMetricRule {
    override fun check(item: HubMetric): MutableList<String> {
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

data class HubMetricRevisionRangeRule(var min: Int, var max: Int) : HubMetricRule {
    override fun check(item: HubMetric): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HubMetricRequiredOwnerIdRule(var message: String) : HubMetricRule {
    override fun check(item: HubMetric): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubMetricLocaleLengthRule(var maxLength: Int) : HubMetricRule {
    override fun check(item: HubMetric): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubMetricCodeLengthRule(var maxLength: Int) : HubMetricRule {
    override fun check(item: HubMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub metric. */
data class HubMetricValidator(var rules: MutableList<HubMetricRule>) {
    fun validate(item: HubMetric): MutableList<String> {
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

    fun requireValid(item: HubMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultHubMetricValidator(): HubMetricValidator {
    return HubMetricValidator(
        mutableListOf<HubMetricRule>(
            HubMetricApprovedGuardRule(1000),
            HubMetricTagsLimitRule(16, mutableListOf<String>("seasonal")),
            HubMetricRevisionRangeRule(3600, 86400),
            HubMetricRequiredOwnerIdRule("hub metric needs an entry"),
            HubMetricLocaleLengthRule(80),
            HubMetricCodeLengthRule(16),
        ),
    )
}
