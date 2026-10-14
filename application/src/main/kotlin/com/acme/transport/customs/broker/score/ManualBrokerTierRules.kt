package com.acme.transport.customs.broker.score

/** A rule a manual broker tier must pass. Check returns its problems, or none. */
interface ManualBrokerTierRule {
    fun check(item: ManualBrokerTier): MutableList<String>
}

data class ManualBrokerTierRequiredLabelRule(var message: String) : ManualBrokerTierRule {
    override fun check(item: ManualBrokerTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerTierEndsAtRangeRule(var min: Int, var max: Int) : ManualBrokerTierRule {
    override fun check(item: ManualBrokerTier): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ManualBrokerTierRequiredExternalRefRule(var message: String) : ManualBrokerTierRule {
    override fun check(item: ManualBrokerTier): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerTierTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ManualBrokerTierRule {
    override fun check(item: ManualBrokerTier): MutableList<String> {
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

/** Runs every rule of a manual broker tier. */
data class ManualBrokerTierValidator(var rules: MutableList<ManualBrokerTierRule>) {
    fun validate(item: ManualBrokerTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualBrokerTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBrokerTier>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBrokerTierValidator(): ManualBrokerTierValidator {
    return ManualBrokerTierValidator(
        mutableListOf<ManualBrokerTierRule>(
            ManualBrokerTierRequiredLabelRule("manual broker tier needs an entry"),
            ManualBrokerTierEndsAtRangeRule(64, 3600),
            ManualBrokerTierRequiredExternalRefRule("manual broker tier needs a value"),
            ManualBrokerTierTagsLimitRule(1000, mutableListOf<String>()),
        ),
    )
}
