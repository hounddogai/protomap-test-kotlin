package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.kit.text.normalize

/** A rule a seasonal inspection draft must pass. Check returns its problems, or none. */
interface SeasonalInspectionDraftRule {
    fun check(item: SeasonalInspectionDraft): MutableList<String>
}

data class SeasonalInspectionDraftPriorityRangeRule(var min: Int, var max: Int) : SeasonalInspectionDraftRule {
    override fun check(item: SeasonalInspectionDraft): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionDraftLimitRangeRule(var min: Int, var max: Int) : SeasonalInspectionDraftRule {
    override fun check(item: SeasonalInspectionDraft): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionDraftAllowedTitleRule(
    var allowed: MutableList<String>,
) : SeasonalInspectionDraftRule {
    override fun check(item: SeasonalInspectionDraft): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionDraftDurationMinutesRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalInspectionDraftRule {
    override fun check(item: SeasonalInspectionDraft): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionDraftRequiredCodeRule(var message: String) : SeasonalInspectionDraftRule {
    override fun check(item: SeasonalInspectionDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalInspectionDraftNotesLengthRule(var maxLength: Int) : SeasonalInspectionDraftRule {
    override fun check(item: SeasonalInspectionDraft): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal inspection draft. */
data class SeasonalInspectionDraftValidator(var rules: MutableList<SeasonalInspectionDraftRule>) {
    fun validate(item: SeasonalInspectionDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalInspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalInspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalInspectionDraftValidator(): SeasonalInspectionDraftValidator {
    return SeasonalInspectionDraftValidator(
        mutableListOf<SeasonalInspectionDraftRule>(
            SeasonalInspectionDraftPriorityRangeRule(120, 1000),
            SeasonalInspectionDraftLimitRangeRule(32, 255),
            SeasonalInspectionDraftAllowedTitleRule(mutableListOf<String>()),
            SeasonalInspectionDraftDurationMinutesRangeRule(16, 3600),
            SeasonalInspectionDraftRequiredCodeRule("seasonal inspection draft needs this field"),
            SeasonalInspectionDraftNotesLengthRule(3600),
        ),
    )
}
