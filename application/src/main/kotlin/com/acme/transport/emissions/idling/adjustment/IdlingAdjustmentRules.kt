package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a idling adjustment must pass. Check returns its problems, or none. */
interface IdlingAdjustmentRule {
    fun check(item: IdlingAdjustment): MutableList<String>
}

data class IdlingAdjustmentDurationMinutesRangeRule(var min: Int, var max: Int) : IdlingAdjustmentRule {
    override fun check(item: IdlingAdjustment): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAdjustmentTimezoneLengthRule(var maxLength: Int) : IdlingAdjustmentRule {
    override fun check(item: IdlingAdjustment): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAdjustmentAllowedTimezoneRule(var allowed: MutableList<String>) : IdlingAdjustmentRule {
    override fun check(item: IdlingAdjustment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAdjustmentVisibleGuardRule(var limit: Int) : IdlingAdjustmentRule {
    override fun check(item: IdlingAdjustment): MutableList<String> {
        if (item.visible && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class IdlingAdjustmentLabelLengthRule(var maxLength: Int) : IdlingAdjustmentRule {
    override fun check(item: IdlingAdjustment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling adjustment. */
data class IdlingAdjustmentValidator(var rules: MutableList<IdlingAdjustmentRule>) {
    fun validate(item: IdlingAdjustment): MutableList<String> {
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

    fun requireValid(item: IdlingAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingAdjustmentValidator(): IdlingAdjustmentValidator {
    return IdlingAdjustmentValidator(
        mutableListOf<IdlingAdjustmentRule>(
            IdlingAdjustmentDurationMinutesRangeRule(120, 3600),
            IdlingAdjustmentTimezoneLengthRule(255),
            IdlingAdjustmentAllowedTimezoneRule(mutableListOf<String>("weighted", "tiered")),
            IdlingAdjustmentVisibleGuardRule(32),
            IdlingAdjustmentLabelLengthRule(255),
        ),
    )
}
