package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.text.normalize

/** A rule a seasonal scorecard assignment must pass. Check returns its problems, or none. */
interface SeasonalScorecardAssignmentRule {
    fun check(item: SeasonalScorecardAssignment): MutableList<String>
}

data class SeasonalScorecardAssignmentCodeLengthRule(var maxLength: Int) : SeasonalScorecardAssignmentRule {
    override fun check(item: SeasonalScorecardAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalScorecardAssignmentNameLengthRule(var maxLength: Int) : SeasonalScorecardAssignmentRule {
    override fun check(item: SeasonalScorecardAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalScorecardAssignmentAllowedCountryRule(
    var allowed: MutableList<String>,
) : SeasonalScorecardAssignmentRule {
    override fun check(item: SeasonalScorecardAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalScorecardAssignmentNotesLengthRule(var maxLength: Int) : SeasonalScorecardAssignmentRule {
    override fun check(item: SeasonalScorecardAssignment): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal scorecard assignment. */
data class SeasonalScorecardAssignmentValidator(var rules: MutableList<SeasonalScorecardAssignmentRule>) {
    fun validate(item: SeasonalScorecardAssignment): MutableList<String> {
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

    fun requireValid(item: SeasonalScorecardAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalScorecardAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalScorecardAssignmentValidator(): SeasonalScorecardAssignmentValidator {
    return SeasonalScorecardAssignmentValidator(
        mutableListOf<SeasonalScorecardAssignmentRule>(
            SeasonalScorecardAssignmentCodeLengthRule(1000),
            SeasonalScorecardAssignmentNameLengthRule(1000),
            SeasonalScorecardAssignmentAllowedCountryRule(mutableListOf<String>("batched", "manual", "lenient")),
            SeasonalScorecardAssignmentNotesLengthRule(3600),
        ),
    )
}
