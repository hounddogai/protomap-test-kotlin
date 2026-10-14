package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.text.normalize

/** A rule a capped availability slot must pass. Check returns its problems, or none. */
interface CappedAvailabilitySlotRule {
    fun check(item: CappedAvailabilitySlot): MutableList<String>
}

data class CappedAvailabilitySlotRequiredTitleRule(var message: String) : CappedAvailabilitySlotRule {
    override fun check(item: CappedAvailabilitySlot): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedAvailabilitySlotDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : CappedAvailabilitySlotRule {
    override fun check(item: CappedAvailabilitySlot): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedAvailabilitySlotAllowedDockRule(
    var allowed: MutableList<String>,
) : CappedAvailabilitySlotRule {
    override fun check(item: CappedAvailabilitySlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class CappedAvailabilitySlotMarginRangeRule(
    var min: Double,
    var max: Double,
) : CappedAvailabilitySlotRule {
    override fun check(item: CappedAvailabilitySlot): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedAvailabilitySlotTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CappedAvailabilitySlotRule {
    override fun check(item: CappedAvailabilitySlot): MutableList<String> {
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

data class CappedAvailabilitySlotTitleLengthRule(var maxLength: Int) : CappedAvailabilitySlotRule {
    override fun check(item: CappedAvailabilitySlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped availability slot. */
data class CappedAvailabilitySlotValidator(var rules: MutableList<CappedAvailabilitySlotRule>) {
    fun validate(item: CappedAvailabilitySlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedAvailabilitySlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedAvailabilitySlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedAvailabilitySlotValidator(): CappedAvailabilitySlotValidator {
    return CappedAvailabilitySlotValidator(
        mutableListOf<CappedAvailabilitySlotRule>(
            CappedAvailabilitySlotRequiredTitleRule("capped availability slot needs this field"),
            CappedAvailabilitySlotDurationMinutesRangeRule(3600, 3600),
            CappedAvailabilitySlotAllowedDockRule(mutableListOf<String>("default", "adaptive")),
            CappedAvailabilitySlotMarginRangeRule(7.7, 2923.9),
            CappedAvailabilitySlotTagsLimitRule(32, mutableListOf<String>("rolling", "tiered", "default")),
            CappedAvailabilitySlotTitleLengthRule(120),
        ),
    )
}
