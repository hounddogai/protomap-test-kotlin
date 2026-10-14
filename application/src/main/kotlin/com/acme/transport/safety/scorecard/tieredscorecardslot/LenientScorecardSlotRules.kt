package com.acme.transport.safety.scorecard.tieredscorecardslot

import com.acme.transport.kit.text.normalize

/** A rule a lenient scorecard slot must pass. Check returns its problems, or none. */
interface LenientScorecardSlotRule {
    fun check(item: LenientScorecardSlot): MutableList<String>
}

data class LenientScorecardSlotNameLengthRule(var maxLength: Int) : LenientScorecardSlotRule {
    override fun check(item: LenientScorecardSlot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientScorecardSlotRetriesRangeRule(var min: Int, var max: Int) : LenientScorecardSlotRule {
    override fun check(item: LenientScorecardSlot): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class LenientScorecardSlotUnitLengthRule(var maxLength: Int) : LenientScorecardSlotRule {
    override fun check(item: LenientScorecardSlot): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientScorecardSlotAllowedUnitRule(var allowed: MutableList<String>) : LenientScorecardSlotRule {
    override fun check(item: LenientScorecardSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient scorecard slot. */
data class LenientScorecardSlotValidator(var rules: MutableList<LenientScorecardSlotRule>) {
    fun validate(item: LenientScorecardSlot): MutableList<String> {
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

    fun requireValid(item: LenientScorecardSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientScorecardSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientScorecardSlotValidator(): LenientScorecardSlotValidator {
    return LenientScorecardSlotValidator(
        mutableListOf<LenientScorecardSlotRule>(
            LenientScorecardSlotNameLengthRule(32),
            LenientScorecardSlotRetriesRangeRule(32, 3600),
            LenientScorecardSlotUnitLengthRule(64),
            LenientScorecardSlotAllowedUnitRule(mutableListOf<String>("seasonal")),
        ),
    )
}
