package com.acme.transport.customs.broker.snapshot

/** A rule a broker snapshot must pass. Check returns its problems, or none. */
interface BrokerSnapshotRule {
    fun check(item: BrokerSnapshot): MutableList<String>
}

data class BrokerSnapshotTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BrokerSnapshotRule {
    override fun check(item: BrokerSnapshot): MutableList<String> {
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

data class BrokerSnapshotRatioRangeRule(var min: Double, var max: Double) : BrokerSnapshotRule {
    override fun check(item: BrokerSnapshot): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BrokerSnapshotPinnedGuardRule(var limit: Double) : BrokerSnapshotRule {
    override fun check(item: BrokerSnapshot): MutableList<String> {
        if (item.pinned && item.ratio > limit) {
            return mutableListOf<String>("ratio ${item.ratio} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a broker snapshot. */
data class BrokerSnapshotValidator(var rules: MutableList<BrokerSnapshotRule>) {
    fun validate(item: BrokerSnapshot): MutableList<String> {
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

    fun requireValid(item: BrokerSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BrokerSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultBrokerSnapshotValidator(): BrokerSnapshotValidator {
    return BrokerSnapshotValidator(
        mutableListOf<BrokerSnapshotRule>(
            BrokerSnapshotTagsLimitRule(80, mutableListOf<String>("pending", "capped", "default")),
            BrokerSnapshotRatioRangeRule(2.5, 2597.1),
            BrokerSnapshotPinnedGuardRule(3.8),
        ),
    )
}
