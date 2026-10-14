package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.text.normalize

/** A rule a global transit time template must pass. Check returns its problems, or none. */
interface GlobalTransitTimeTemplateRule {
    fun check(item: GlobalTransitTimeTemplate): MutableList<String>
}

data class GlobalTransitTimeTemplateAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : GlobalTransitTimeTemplateRule {
    override fun check(item: GlobalTransitTimeTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is missing")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeTemplateGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : GlobalTransitTimeTemplateRule {
    override fun check(item: GlobalTransitTimeTemplate): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeTemplateRequiredOwnerIdRule(var message: String) : GlobalTransitTimeTemplateRule {
    override fun check(item: GlobalTransitTimeTemplate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalTransitTimeTemplateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : GlobalTransitTimeTemplateRule {
    override fun check(item: GlobalTransitTimeTemplate): MutableList<String> {
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

/** Runs every rule of a global transit time template. */
data class GlobalTransitTimeTemplateValidator(var rules: MutableList<GlobalTransitTimeTemplateRule>) {
    fun validate(item: GlobalTransitTimeTemplate): MutableList<String> {
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

    fun requireValid(item: GlobalTransitTimeTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalTransitTimeTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalTransitTimeTemplateValidator(): GlobalTransitTimeTemplateValidator {
    return GlobalTransitTimeTemplateValidator(
        mutableListOf<GlobalTransitTimeTemplateRule>(
            GlobalTransitTimeTemplateAllowedOwnerIdRule(mutableListOf<String>("tiered")),
            GlobalTransitTimeTemplateGraceDaysRangeRule(32, 1000),
            GlobalTransitTimeTemplateRequiredOwnerIdRule("global transit time template needs this field"),
            GlobalTransitTimeTemplateTagsLimitRule(80, mutableListOf<String>()),
        ),
    )
}
