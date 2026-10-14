package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.normalize

/** A rule a availability draft must pass. Check returns its problems, or none. */
interface AvailabilityDraftRule {
    fun check(item: AvailabilityDraft): MutableList<String>
}

data class AvailabilityDraftBatchSizeRangeRule(var min: Int, var max: Int) : AvailabilityDraftRule {
    override fun check(item: AvailabilityDraft): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class AvailabilityDraftRequiredLaneRule(var message: String) : AvailabilityDraftRule {
    override fun check(item: AvailabilityDraft): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityDraftAllowedLaneRule(var allowed: MutableList<String>) : AvailabilityDraftRule {
    override fun check(item: AvailabilityDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.lane))) {
            return mutableListOf<String>("lane ${item.lane} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class AvailabilityDraftTitleLengthRule(var maxLength: Int) : AvailabilityDraftRule {
    override fun check(item: AvailabilityDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a availability draft. */
data class AvailabilityDraftValidator(var rules: MutableList<AvailabilityDraftRule>) {
    fun validate(item: AvailabilityDraft): MutableList<String> {
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

    fun requireValid(item: AvailabilityDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AvailabilityDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultAvailabilityDraftValidator(): AvailabilityDraftValidator {
    return AvailabilityDraftValidator(
        mutableListOf<AvailabilityDraftRule>(
            AvailabilityDraftBatchSizeRangeRule(16, 1000),
            AvailabilityDraftRequiredLaneRule("availability draft needs an entry"),
            AvailabilityDraftAllowedLaneRule(mutableListOf<String>("tiered", "regional", "legacy")),
            AvailabilityDraftTitleLengthRule(80),
        ),
    )
}
