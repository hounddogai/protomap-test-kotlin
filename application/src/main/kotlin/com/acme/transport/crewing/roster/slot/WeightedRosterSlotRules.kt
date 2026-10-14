package com.acme.transport.crewing.roster.slot

/** A rule a weighted roster slot must pass. Check returns its problems, or none. */
interface WeightedRosterSlotRule {
    fun check(item: WeightedRosterSlot): MutableList<String>
}

data class WeightedRosterSlotTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : WeightedRosterSlotRule {
    override fun check(item: WeightedRosterSlot): MutableList<String> {
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

data class WeightedRosterSlotRequiredTitleRule(var message: String) : WeightedRosterSlotRule {
    override fun check(item: WeightedRosterSlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterSlotCodePrefixRule(var prefix: String) : WeightedRosterSlotRule {
    override fun check(item: WeightedRosterSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterSlotStartsAtRangeRule(var min: Int, var max: Int) : WeightedRosterSlotRule {
    override fun check(item: WeightedRosterSlot): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class WeightedRosterSlotBudgetRangeRule(var min: Double, var max: Double) : WeightedRosterSlotRule {
    override fun check(item: WeightedRosterSlot): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class WeightedRosterSlotRequiredNameRule(var message: String) : WeightedRosterSlotRule {
    override fun check(item: WeightedRosterSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted roster slot. */
data class WeightedRosterSlotValidator(var rules: MutableList<WeightedRosterSlotRule>) {
    fun validate(item: WeightedRosterSlot): MutableList<String> {
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

    fun requireValid(item: WeightedRosterSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRosterSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRosterSlotValidator(): WeightedRosterSlotValidator {
    return WeightedRosterSlotValidator(
        mutableListOf<WeightedRosterSlotRule>(
            WeightedRosterSlotTagsLimitRule(16, mutableListOf<String>("regional", "legacy", "lenient")),
            WeightedRosterSlotRequiredTitleRule("weighted roster slot needs this field"),
            WeightedRosterSlotCodePrefixRule("OPS"),
            WeightedRosterSlotStartsAtRangeRule(32, 3600),
            WeightedRosterSlotBudgetRangeRule(3.9, 1448.4),
            WeightedRosterSlotRequiredNameRule("weighted roster slot needs a value"),
        ),
    )
}
