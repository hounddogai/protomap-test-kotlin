package com.acme.transport.crewing.swap.slot

/** A rule a tiered swap slot must pass. Check returns its problems, or none. */
interface TieredSwapSlotRule {
    fun check(item: TieredSwapSlot): MutableList<String>
}

data class TieredSwapSlotTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : TieredSwapSlotRule {
    override fun check(item: TieredSwapSlot): MutableList<String> {
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

data class TieredSwapSlotCapacityRangeRule(var min: Int, var max: Int) : TieredSwapSlotRule {
    override fun check(item: TieredSwapSlot): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class TieredSwapSlotRequiredDescriptionRule(var message: String) : TieredSwapSlotRule {
    override fun check(item: TieredSwapSlot): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered swap slot. */
data class TieredSwapSlotValidator(var rules: MutableList<TieredSwapSlotRule>) {
    fun validate(item: TieredSwapSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredSwapSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSwapSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSwapSlotValidator(): TieredSwapSlotValidator {
    return TieredSwapSlotValidator(
        mutableListOf<TieredSwapSlotRule>(
            TieredSwapSlotTagsLimitRule(255, mutableListOf<String>()),
            TieredSwapSlotCapacityRangeRule(32, 80),
            TieredSwapSlotRequiredDescriptionRule("tiered swap slot needs an entry"),
        ),
    )
}
