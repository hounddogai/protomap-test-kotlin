package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.normalize

/** A rule a fallback qualification slot must pass. Check returns its problems, or none. */
interface FallbackQualificationSlotRule {
    fun check(item: FallbackQualificationSlot): MutableList<String>
}

data class FallbackQualificationSlotTitleLengthRule(var maxLength: Int) : FallbackQualificationSlotRule {
    override fun check(item: FallbackQualificationSlot): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSlotNotesLengthRule(var maxLength: Int) : FallbackQualificationSlotRule {
    override fun check(item: FallbackQualificationSlot): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSlotThresholdRangeRule(
    var min: Double,
    var max: Double,
) : FallbackQualificationSlotRule {
    override fun check(item: FallbackQualificationSlot): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSlotDriverIdLengthRule(var maxLength: Int) : FallbackQualificationSlotRule {
    override fun check(item: FallbackQualificationSlot): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSlotAllowedTrailerIdRule(
    var allowed: MutableList<String>,
) : FallbackQualificationSlotRule {
    override fun check(item: FallbackQualificationSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class FallbackQualificationSlotGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackQualificationSlotRule {
    override fun check(item: FallbackQualificationSlot): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback qualification slot. */
data class FallbackQualificationSlotValidator(var rules: MutableList<FallbackQualificationSlotRule>) {
    fun validate(item: FallbackQualificationSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackQualificationSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackQualificationSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackQualificationSlotValidator(): FallbackQualificationSlotValidator {
    return FallbackQualificationSlotValidator(
        mutableListOf<FallbackQualificationSlotRule>(
            FallbackQualificationSlotTitleLengthRule(255),
            FallbackQualificationSlotNotesLengthRule(86400),
            FallbackQualificationSlotThresholdRangeRule(1.8, 4335.6),
            FallbackQualificationSlotDriverIdLengthRule(80),
            FallbackQualificationSlotAllowedTrailerIdRule(mutableListOf<String>("batched")),
            FallbackQualificationSlotGraceDaysRangeRule(80, 1000),
        ),
    )
}
