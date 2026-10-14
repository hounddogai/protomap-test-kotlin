package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.kit.text.normalize

/** A rule a attempt slot must pass. Check returns its problems, or none. */
interface AttemptSlotRule {
    fun check(item: AttemptSlot): MutableList<String>
}

data class AttemptSlotAllowedTitleRule(var allowed: MutableList<String>) : AttemptSlotRule {
    override fun check(item: AttemptSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is missing")
        }
        return mutableListOf<String>()
    }
}

data class AttemptSlotCodeLengthRule(var maxLength: Int) : AttemptSlotRule {
    override fun check(item: AttemptSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptSlotUpdatedAtRangeRule(var min: Int, var max: Int) : AttemptSlotRule {
    override fun check(item: AttemptSlot): MutableList<String> {
        val value = item.updatedAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("updated at $value is below $min")
        }
        if (value > max) {
            problems.add("updated at $value is above $max")
        }
        return problems
    }
}

data class AttemptSlotTitleLengthRule(var maxLength: Int) : AttemptSlotRule {
    override fun check(item: AttemptSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt slot. */
data class AttemptSlotValidator(var rules: MutableList<AttemptSlotRule>) {
    fun validate(item: AttemptSlot): MutableList<String> {
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

    fun requireValid(item: AttemptSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptSlotValidator(): AttemptSlotValidator {
    return AttemptSlotValidator(
        mutableListOf<AttemptSlotRule>(
            AttemptSlotAllowedTitleRule(mutableListOf<String>("pending")),
            AttemptSlotCodeLengthRule(64),
            AttemptSlotUpdatedAtRangeRule(80, 255),
            AttemptSlotTitleLengthRule(120),
        ),
    )
}
